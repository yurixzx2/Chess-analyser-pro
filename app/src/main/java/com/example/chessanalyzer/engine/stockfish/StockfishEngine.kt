package com.example.chessanalyzer.engine.stockfish

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.concurrent.Executors
import java.util.concurrent.Future

data class AnalysisResult(
    val bestMove: String?,
    val ponderMove: String?,
    val scoreCp: Int?, // Centipawns
    val scoreMate: Int?, // Mate in N moves
    val pv: List<String> // Principal Variation
)

class StockfishEngine {

    private var process: Process? = null
    private var reader: BufferedReader? = null
    private var writer: OutputStreamWriter? = null
    private val executor = Executors.newSingleThreadExecutor()
    private var analysisFuture: Future<*>? = null

    init {
        start()
    }

    fun start() {
        try {
            // Assuming libstockfish.so is in the app's native libraries directory
            // and can be executed directly. This might need adjustment based on Android's NDK setup.
            // For AndroidIDE, the binary might need to be placed in a specific location or accessed differently.
            process = Runtime.getRuntime().exec("stockfish") // Placeholder, actual path might vary
            reader = BufferedReader(InputStreamReader(process!!.inputStream))
            writer = OutputStreamWriter(process!!.outputStream)

            // Set UCI mode
            sendCommand("uci")
            // Wait for "uciok"
            readOutputUntil("uciok")

            // Initialize engine
            sendCommand("isready")
            readOutputUntil("readyok")

            println("Stockfish engine started and ready.")
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error starting Stockfish engine: ${e.message}")
        }
    }

    fun sendCommand(command: String) {
        writer?.apply {
            write(command + "\n")
            flush()
        }
    }

    fun readOutputUntil(expectedString: String, timeoutMillis: Long = 5000): String {
        val startTime = System.currentTimeMillis()
        val output = StringBuilder()
        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            if (reader?.ready() == true) {
                val line = reader?.readLine()
                if (line != null) {
                    output.append(line).append("\n")
                    if (line.contains(expectedString)) {
                        return output.toString()
                    }
                }
            }
            Thread.sleep(10)
        }
        return output.toString()
    }

    fun analyzePosition(fen: String, depth: Int, onResult: (AnalysisResult) -> Unit) {
        analysisFuture?.cancel(true)
        analysisFuture = executor.submit {
            try {
                sendCommand("position fen $fen")
                sendCommand("go depth $depth")

                val output = readOutputUntil("bestmove")
                val result = parseStockfishOutput(output)
                onResult(result)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(AnalysisResult(null, null, null, null, emptyList()))
            }
        }
    }

    private fun parseStockfishOutput(output: String): AnalysisResult {
        var bestMove: String? = null
        var ponderMove: String? = null
        var scoreCp: Int? = null
        var scoreMate: Int? = null
        val pv = mutableListOf<String>()

        output.split("\n").forEach { line ->
            if (line.startsWith("info")) {
                val parts = line.split(" ")
                val scoreIndex = parts.indexOf("score")
                if (scoreIndex != -1 && scoreIndex + 2 < parts.size) {
                    when (parts[scoreIndex + 1]) {
                        "cp" -> scoreCp = parts[scoreIndex + 2].toInt()
                        "mate" -> scoreMate = parts[scoreIndex + 2].toInt()
                    }
                }
                val pvIndex = parts.indexOf("pv")
                if (pvIndex != -1 && pvIndex + 1 < parts.size) {
                    for (i in pvIndex + 1 until parts.size) {
                        pv.add(parts[i])
                    }
                }
            } else if (line.startsWith("bestmove")) {
                val parts = line.split(" ")
                bestMove = parts.getOrNull(1)
                ponderMove = parts.getOrNull(3)
            }
        }
        return AnalysisResult(bestMove, ponderMove, scoreCp, scoreMate, pv)
    }

    fun stop() {
        analysisFuture?.cancel(true)
        sendCommand("stop")
        sendCommand("quit")
        process?.destroy()
        executor.shutdownNow()
        println("Stockfish engine stopped.")
    }
}
