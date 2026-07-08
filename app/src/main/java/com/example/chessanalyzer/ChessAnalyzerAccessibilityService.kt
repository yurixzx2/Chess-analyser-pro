package com.example.chessanalyzer

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.TextView
import com.example.chessanalyzer.engine.ChessEngineManager
import com.example.chessanalyzer.engine.maia.MaiaEngine
import com.example.chessanalyzer.engine.stockfish.StockfishEngine

class ChessAnalyzerAccessibilityService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var overlayView: android.view.View? = null
    private var evaluationTextView: TextView? = null
    private var moveClassificationTextView: TextView? = null

    private lateinit var chessEngineManager: ChessEngineManager

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        chessEngineManager = ChessEngineManager(StockfishEngine(), MaiaEngine(applicationContext))
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: return

        // Check if the event is from Chess.com or Lichess
        if (packageName.contains("com.chess") || packageName.contains("com.lichess")) {
            // Placeholder for board reading logic
            // In a real implementation, this would involve traversing the accessibility tree
            // to identify chess board elements and extract the FEN string.
            // For now, we'll just show the overlay.
            showOverlay("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1") // Example FEN

            // Placeholder for engine analysis
            chessEngineManager.getBestMove("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 10, com.example.chessanalyzer.engine.StyleMode.DEFAULT) {
                // Update overlay with analysis result
                evaluationTextView?.text = "Eval: N/A"
                moveClassificationTextView?.text = "Best"
            }
        }
    }

    private fun showOverlay(fen: String) {
        if (overlayView == null) {
            overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)
            evaluationTextView = overlayView?.findViewById(R.id.evaluationTextView)
            moveClassificationTextView = overlayView?.findViewById(R.id.moveClassificationTextView)

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            )
            params.gravity = Gravity.TOP or Gravity.START
            params.x = 0
            params.y = 0

            windowManager?.addView(overlayView, params)
        }
        // Update overlay content based on FEN or analysis results
        // This will be implemented in detail later
    }

    private fun hideOverlay() {
        if (overlayView != null) {
            windowManager?.removeView(overlayView)
            overlayView = null
        }
    }

    override fun onInterrupt() {
        // Called when the service is interrupted by the system or another accessibility service
        hideOverlay()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        info.packageNames = arrayOf("com.chess", "com.lichess") // Target specific apps
        info.notificationTimeout = 100
        this.serviceInfo = info

        println("ChessAnalyzerAccessibilityService connected")
    }

    override fun onDestroy() {
        super.onDestroy()
        hideOverlay()
        chessEngineManager.stopEngines()
    }
}
