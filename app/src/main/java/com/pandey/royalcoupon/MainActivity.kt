package com.pandey.royalcoupon

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var codesInput: EditText
    private lateinit var delayInput: EditText
    private lateinit var statusText: TextView
    private lateinit var progressText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        codesInput = findViewById(R.id.codesInput)
        delayInput = findViewById(R.id.delayInput)
        statusText = findViewById(R.id.statusText)
        progressText = findViewById(R.id.progressText)

        findViewById<Button>(R.id.startButton).setOnClickListener {
            val codes = codesInput.text.toString()
                .lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .take(200)

            if (codes.isEmpty()) {
                statusText.text = "Status: Add at least one code."
                return@setOnClickListener
            }

            val delayMs = ((delayInput.text.toString().toDoubleOrNull() ?: 3.0) * 1000)
                .toLong().coerceAtLeast(500L)

            CouponAccessibilityService.startAutomation(codes, delayMs)
            statusText.text = "Status: Started"
        }

        findViewById<Button>(R.id.pauseButton).setOnClickListener {
            CouponAccessibilityService.pauseAutomation()
            statusText.text = "Status: Paused"
        }

        findViewById<Button>(R.id.stopButton).setOnClickListener {
            CouponAccessibilityService.stopAutomation()
            statusText.text = "Status: Stopped"
        }

        findViewById<Button>(R.id.accessibilityButton).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        CouponAccessibilityService.onProgress = { current, total, status ->
            runOnUiThread {
                progressText.text = "$current / $total"
                statusText.text = "Status: $status"
            }
        }
    }
}
