package com.example.chessanalyzer

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.content.ComponentName
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider

class MainActivity : AppCompatActivity() {

    private lateinit var depthSlider: Slider
    private lateinit var eloEstimatedTextView: TextView
    private lateinit var showArrowsToggle: MaterialSwitch
    private lateinit var showEnemyBestMoveToggle: MaterialSwitch
    private lateinit var hideSuggestionsToggle: MaterialSwitch
    private lateinit var analyzeMyMovesToggle: MaterialSwitch
    private lateinit var showEvaluationToggle: MaterialSwitch
    private lateinit var moveNotationRadioGroup: RadioGroup
    private lateinit var endgameBoostToggle: MaterialSwitch
    private lateinit var autoPlayToggle: MaterialSwitch
    private lateinit var styleModeSpinner: Spinner
    private lateinit var startButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        depthSlider = findViewById(R.id.depthSlider)
        eloEstimatedTextView = findViewById(R.id.eloEstimatedTextView)
        showArrowsToggle = findViewById(R.id.showArrowsToggle)
        showEnemyBestMoveToggle = findViewById(R.id.showEnemyBestMoveToggle)
        hideSuggestionsToggle = findViewById(R.id.hideSuggestionsToggle)
        analyzeMyMovesToggle = findViewById(R.id.analyzeMyMovesToggle)
        showEvaluationToggle = findViewById(R.id.showEvaluationToggle)
        moveNotationRadioGroup = findViewById(R.id.moveNotationRadioGroup)
        endgameBoostToggle = findViewById(R.id.endgameBoostToggle)
        autoPlayToggle = findViewById(R.id.autoPlayToggle)
        styleModeSpinner = findViewById(R.id.styleModeSpinner)
        startButton = findViewById(R.id.startButton)

        setupListeners()
        setupStyleModeSpinner()
    }

    private fun setupListeners() {
        depthSlider.addOnChangeListener { slider, value, fromUser ->
            // Update ELO estimated based on depth
            val elo = calculateEloFromDepth(value.toInt())
            eloEstimatedTextView.text = getString(R.string.elo_estimated_label) + " $elo"
        }

        startButton.setOnClickListener { 
            if (isAccessibilityServiceEnabled()) {
                // Start the accessibility service
                val serviceIntent = Intent(this, ChessAnalyzerAccessibilityService::class.java)
                startService(serviceIntent)
                Toast.makeText(this, "Serviço TAL PRO V3 iniciado!", Toast.LENGTH_SHORT).show()
            } else {
                // Prompt user to enable accessibility service
                Toast.makeText(this, "Por favor, habilite o serviço de acessibilidade TAL PRO V3.", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
        }
    }

    private fun setupStyleModeSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.style_modes,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        styleModeSpinner.adapter = adapter
    }

    private fun calculateEloFromDepth(depth: Int): Int {
        // Simple placeholder for ELO calculation based on depth
        // This can be made more sophisticated based on actual engine performance data
        return 800 + (depth * 100)
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val accessibilityServiceId = ComponentName(this, ChessAnalyzerAccessibilityService::class.java).flattenToString()
        val enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        return enabledServices?.contains(accessibilityServiceId) == true
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop the accessibility service when the main activity is destroyed
        val serviceIntent = Intent(this, ChessAnalyzerAccessibilityService::class.java)
        stopService(serviceIntent)
    }
}
