package com.example.chessanalyzer.engine.maia

import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

object FenProcessor {

    private const val BOARD_SIZE = 8
    private const val NUM_PIECE_PLANES = 12 // 6 white + 6 black
    private const val NUM_AUX_PLANES = 8
    private const val HISTORY_PLANES_PER_POSITION = 13 // 6 white + 6 black + repetition
    private const val HISTORY_DEPTH = 8 // 8 historical positions

    // Maps FEN piece chars to their index in the one-hot encoding
    private val pieceToIndex = mapOf(
        'P' to 0, 'N' to 1, 'B' to 2, 'R' to 3, 'Q' to 4, 'K' to 5,
        'p' to 6, 'n' to 7, 'b' to 8, 'r' to 9, 'q' to 10, 'k' to 11
    )

    fun fenToTensor(fen: String): ByteBuffer {
        val parts = fen.split(" ")
        val boardFen = parts[0]
        val activeColor = parts[1]
        val castlingRights = parts[2]
        val enPassant = parts[3]
        val halfmoveClock = parts[4].toInt()
        val fullmoveNumber = parts[5].toInt()

        // Total planes: 104 (history) + 8 (auxiliary) = 112
        val bufferSize = BOARD_SIZE * BOARD_SIZE * (HISTORY_PLANES_PER_POSITION * HISTORY_DEPTH + NUM_AUX_PLANES) * 4 // 4 bytes per float
        val byteBuffer = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder())

        // Initialize all planes to 0
        for (i in 0 until bufferSize / 4) {
            byteBuffer.putFloat(0f)
        }
        byteBuffer.rewind()

        // Process board state for current position (first 12 planes of history)
        var rank = 7
        var file = 0
        for (char in boardFen) {
            when (char) {
                '/' -> {
                    rank--
                    file = 0
                }
                in '1'..'8' -> file += char.toString().toInt()
                else -> {
                    val pieceIndex = pieceToIndex[char]
                    if (pieceIndex != null) {
                        // For the current position, fill the first 12 planes (0-11)
                        val planeIndex = pieceIndex
                        val bufferPosition = (planeIndex * BOARD_SIZE * BOARD_SIZE + (7 - rank) * BOARD_SIZE + file) * 4
                        byteBuffer.putFloat(bufferPosition, 1f)
                    }
                    file++
                }
            }
        }

        // Auxiliary planes (last 8 planes)
        // Plane 104: active color (1 for white, 0 for black)
        val activeColorPlaneIndex = HISTORY_PLANES_PER_POSITION * HISTORY_DEPTH
        if (activeColor == "w") {
            for (i in 0 until BOARD_SIZE * BOARD_SIZE) {
                byteBuffer.putFloat((activeColorPlaneIndex * BOARD_SIZE * BOARD_SIZE + i) * 4, 1f)
            }
        }

        // Plane 105: castling rights (white kingside)
        val wkCastlingPlaneIndex = activeColorPlaneIndex + 1
        if (castlingRights.contains('K')) {
            for (i in 0 until BOARD_SIZE * BOARD_SIZE) {
                byteBuffer.putFloat((wkCastlingPlaneIndex * BOARD_SIZE * BOARD_SIZE + i) * 4, 1f)
            }
        }

        // Plane 106: castling rights (white queenside)
        val wqCastlingPlaneIndex = activeColorPlaneIndex + 2
        if (castlingRights.contains('Q')) {
            for (i in 0 until BOARD_SIZE * BOARD_SIZE) {
                byteBuffer.putFloat((wqCastlingPlaneIndex * BOARD_SIZE * BOARD_SIZE + i) * 4, 1f)
            }
        }

        // Plane 107: castling rights (black kingside)
        val bkCastlingPlaneIndex = activeColorPlaneIndex + 3
        if (castlingRights.contains('k')) {
            for (i in 0 until BOARD_SIZE * BOARD_SIZE) {
                byteBuffer.putFloat((bkCastlingPlaneIndex * BOARD_SIZE * BOARD_SIZE + i) * 4, 1f)
            }
        }

        // Plane 108: castling rights (black queenside)
        val bqCastlingPlaneIndex = activeColorPlaneIndex + 4
        if (castlingRights.contains('q')) {
            for (i in 0 until BOARD_SIZE * BOARD_SIZE) {
                byteBuffer.putFloat((bqCastlingPlaneIndex * BOARD_SIZE * BOARD_SIZE + i) * 4, 1f)
            }
        }

        // Plane 109: en passant square
        val enPassantPlaneIndex = activeColorPlaneIndex + 5
        if (enPassant != "-") {
            val epFile = enPassant[0] - 'a'
            val epRank = enPassant[1].toString().toInt() - 1
            val bufferPosition = (enPassantPlaneIndex * BOARD_SIZE * BOARD_SIZE + (7 - epRank) * BOARD_SIZE + epFile) * 4
            byteBuffer.putFloat(bufferPosition, 1f)
        }

        // Plane 110: halfmove clock (repeated across all squares)
        val halfmoveClockPlaneIndex = activeColorPlaneIndex + 6
        val halfmoveValue = halfmoveClock.toFloat() / 100f // Normalize if needed, Maia usually expects 0-1 range
        for (i in 0 until BOARD_SIZE * BOARD_SIZE) {
            byteBuffer.putFloat((halfmoveClockPlaneIndex * BOARD_SIZE * BOARD_SIZE + i) * 4, halfmoveValue)
        }

        // Plane 111: fullmove number (repeated across all squares)
        val fullmoveNumberPlaneIndex = activeColorPlaneIndex + 7
        val fullmoveValue = fullmoveNumber.toFloat() / 100f // Normalize if needed
        for (i in 0 until BOARD_SIZE * BOARD_SIZE) {
            byteBuffer.putFloat((fullmoveNumberPlaneIndex * BOARD_SIZE * BOARD_SIZE + i) * 4, fullmoveValue)
        }

        return byteBuffer
    }

    fun indexToMove(index: Int): String {
        if (index < 0 || index >= policyIndex.size) {
            throw IllegalArgumentException("Index fora dos limites: $index")
        }
        return policyIndex[index]
    }
}
