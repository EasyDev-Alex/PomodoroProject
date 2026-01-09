package com.example.pomodoroproject

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class StatsActivity : AppCompatActivity() {

    private lateinit var tvCompleted: TextView
    private lateinit var btnResetStats: Button
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        tvCompleted = findViewById(R.id.tvCompleted)
        btnResetStats = findViewById(R.id.btnResetStats)
        btnBack = findViewById(R.id.btnBack)

        loadStats()

        btnResetStats.setOnClickListener {
            resetStats()
        }
        btnBack.setOnClickListener {
            finish()
        }

    }

    private fun loadStats() {
        val prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)
        val completed = prefs.getInt("completed_sessions", 0)
        tvCompleted.text = "Completed Pomodoros: $completed"
    }

    private fun StatsActivity.resetStats() {
        val prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putInt("completed_sessions", 0).apply()
        loadStats()
    }
}


