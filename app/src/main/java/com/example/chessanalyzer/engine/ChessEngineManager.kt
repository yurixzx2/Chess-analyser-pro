package com.example.chessanalyzer.engine

import com.example.chessanalyzer.engine.maia.MaiaEngine
import com.example.chessanalyzer.engine.stockfish.StockfishEngine
import com.example.chessanalyzer.engine.stockfish.AnalysisResult

enum class StyleMode {
    DEFAULT, AGGRESSIVE, DEFENSIVE, TRADER, HUMAN, CRAZY
}

class ChessEngineManager(
    private val stockfishEngine: StockfishEngine,
    private val maiaEngine: MaiaEngine
) {

    fun getBestMove(
        fen: String,
        depth: Int,
        styleMode: StyleMode,
        onResult: (String?) -> Unit
    ) {
        when (styleMode) {
            StyleMode.DEFAULT -> {
                stockfishEngine.analyzePosition(fen, depth) { result ->
                    onResult(result.bestMove)
                }
            }
            StyleMode.HUMAN -> {
                val maiaMove = maiaEngine.predictMove(fen)
                onResult(maiaMove)
            }
            StyleMode.AGGRESSIVE -> {
                // Placeholder for aggressive style logic
                // In a real implementation, this would involve more complex Stockfish commands
                // or analysis of multiple lines to find aggressive moves (e.g., checks, captures, threats).
                stockfishEngine.analyzePosition(fen, depth) { result ->
                    // For now, just return Stockfish's best move as a placeholder
                    // A more advanced implementation would analyze result.pv for aggressive moves
                    onResult(result.bestMove)
                }
            }
            StyleMode.DEFENSIVE -> {
                // Placeholder for defensive style logic
                // This would involve prioritizing moves that improve king safety, pawn structure, etc.
                stockfishEngine.analyzePosition(fen, depth) { result ->
                    // For now, just return Stockfish's best move as a placeholder
                    onResult(result.bestMove)
                }
            }
            StyleMode.TRADER -> {
                // Placeholder for trader style logic
                // This would involve prioritizing moves that lead to piece exchanges.
                stockfishEngine.analyzePosition(fen, depth) { result ->
                    // For now, just return Stockfish's best move as a placeholder
                    onResult(result.bestMove)
                }
            }
            StyleMode.CRAZY -> {
                // Placeholder for crazy style logic
                // This would involve looking for tactical sacrifices or gambits.
                stockfishEngine.analyzePosition(fen, depth) { result ->
                    // For now, just return Stockfish's best move as a placeholder
                    onResult(result.bestMove)
                }
            }
        }
    }

    fun stopEngines() {
        stockfishEngine.stop()
        maiaEngine.close()
    }
}
