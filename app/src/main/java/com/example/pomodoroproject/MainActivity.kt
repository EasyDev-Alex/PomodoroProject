package com.example.pomodoroproject

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    companion object {
        const val PREFS_NAME = "pomodoro_prefs"
        const val KEY_WORK = "work_minutes"
        const val KEY_BREAK = "break_minutes"
        const val KEY_LONG_BREAK = "long_break_minutes"
    }

    private lateinit var tvTimer: TextView
    private lateinit var tvSessionType: TextView
    private lateinit var btnStartPause: Button
    private lateinit var btnReset: Button
    private lateinit var timerCard: CardView
    private lateinit var btnSettings: Button
    private lateinit var btnStats: Button
    private var isPaused = false

    private var countDownTimer: CountDownTimer? = null
    private var isRunning = false
    private var isWorkSession = true
    private var isWaitingForBreak = false

    private var completedPomodoros = 0
    private var isLongBreak = false

    private var workTimeMillis = 25 * 60 * 1000L
    private var breakTimeMillis = 5 * 60 * 1000L
    private var longBreakTimeMillis = 15 * 60 * 1000L // 15 minuta po defaultu
    private var timeLeftMillis = workTimeMillis


    override fun onCreate(savedInstanceState: Bundle?) {



        super.onCreate(savedInstanceState)


        loadSettings()

        setContentView(R.layout.activity_main)
        tvTimer = findViewById(R.id.tvTimer)
        tvSessionType = findViewById(R.id.tvSessionType)
        btnStartPause = findViewById(R.id.btnStartPause)
        btnReset = findViewById(R.id.btnReset)
        timerCard = findViewById(R.id.timerCard)
        btnSettings = findViewById(R.id.btnSettings)
        btnStats = findViewById(R.id.btnStats)



        btnStartPause.setOnClickListener {
            when {
                isRunning -> pauseTimer()
                isPaused -> resumeTimer()
                isWaitingForBreak -> {
                    isWaitingForBreak = false
                    startTimer()
                }
                else -> startTimer()
            }
        }

        btnReset.setOnClickListener {
            resetTimer()
        }
        btnReset.setOnLongClickListener {
            switchSession()
            true
        }
        btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        btnStats.setOnClickListener {
            val intent = Intent(this, StatsActivity::class.java)
            startActivity(intent)
        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }



    override fun onResume() {
        super.onResume()

        loadSettings()

        val restored = restoreTimerState()

        if (!isRunning && !restored) {
            timeLeftMillis = when {
                isWorkSession -> workTimeMillis
                isLongBreak -> longBreakTimeMillis
                else -> breakTimeMillis
            }

            updateSessionLabel()
            updateTimerText()
            updateTimerColor()

        }
    }

    override fun onPause() {
        super.onPause()
        saveTimerState()

    }


    private fun loadSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        workTimeMillis = prefs.getInt(KEY_WORK, 25) * 60 * 1000L
        breakTimeMillis = prefs.getInt(KEY_BREAK, 5) * 60 * 1000L
        longBreakTimeMillis = prefs.getInt(KEY_LONG_BREAK, 15) * 60 * 1000L

        if (!isRunning && !isPaused) {
            timeLeftMillis = when {
                isWorkSession -> workTimeMillis
                isLongBreak -> longBreakTimeMillis
                else -> breakTimeMillis
            }
        }

    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftMillis = millisUntilFinished
                updateTimerText()
            }

            override fun onFinish() {
                isRunning = false
                isPaused = false
                timeLeftMillis = 0
                updateTimerText()

                if (isWorkSession) {
                    incrementCompletedSessions()
                    completedPomodoros++

                    isLongBreak = completedPomodoros % 4 == 0

                    // Spremanje break-a
                    isWorkSession = false
                    timeLeftMillis = if (isLongBreak) {
                        longBreakTimeMillis
                    } else {
                        breakTimeMillis
                    }
                    updateTimerColor()
                    updateTimerText()

                    isWaitingForBreak = true
                    btnStartPause.text = if (isLongBreak) "Start Long Break" else "Start Break"

                } else {
                    // Break zavrsen
                    isWorkSession = true
                    isWaitingForBreak = false
                    isLongBreak = false
                    timeLeftMillis = workTimeMillis
                    updateTimerColor()
                    updateTimerText()

                    btnStartPause.text = "Start"

                }
            }

            private fun incrementCompletedSessions() {
                val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                val completed = prefs.getInt("completed_sessions", 0)
                prefs.edit()
                    .putInt("completed_sessions", completed + 1)
                    .apply()
            }
        }.start()

        isRunning = true
        isPaused = false
        btnStartPause.text = "Pause"
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isRunning = false
        isPaused = true
        btnStartPause.text = "Resume"
    }

    private fun resumeTimer() {
        startTimer()
    }

    private fun resetTimer() {
        countDownTimer?.cancel()

        isRunning = false
        isPaused = false
        isWaitingForBreak = false

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().remove("time_left").apply()

        timeLeftMillis = when {
            isWorkSession -> workTimeMillis
            isLongBreak -> longBreakTimeMillis
            else -> breakTimeMillis
        }

        btnStartPause.text = "Start"

        updateSessionLabel()
        updateTimerText()
    }

    private fun switchSession() {
        countDownTimer?.cancel()
        isRunning = false
        isPaused = false
        isWaitingForBreak = false
        isLongBreak = false



        isWorkSession = !isWorkSession
        timeLeftMillis = if (isWorkSession) workTimeMillis else breakTimeMillis

        animateSessionChange()
        updateSessionLabel()
        updateTimerText()
        updateTimerColor()
        btnStartPause.text = "Start"
    }


    private fun updateTimerColor() {
        val colorRes = when {
            isWorkSession -> R.color.work_red
            isLongBreak -> R.color.long_break_blue
            else -> R.color.break_green
        }
        timerCard.setCardBackgroundColor(
            ContextCompat.getColor(this, colorRes)
        )
    }

    private fun updateTimerText() {
        val minutes = (timeLeftMillis / 1000) / 60
        val seconds = (timeLeftMillis / 1000) % 60

        val newText = String.format("%02d:%02d", minutes, seconds)

        if (tvTimer.text != newText) {
            tvTimer.animate()
                .alpha(0.7f)
                .setDuration(80)
                .withEndAction {
                    tvTimer.text = newText
                    tvTimer.animate()
                        .alpha(1f)
                        .setDuration(80)
                }
        }
    }

    private fun updateSessionLabel() {
        tvSessionType.text = when {
            isWorkSession -> "Work Session"
            isLongBreak -> "Long Break"
            else -> "Break"
        }

        tvSessionType.setTextColor(
            ContextCompat.getColor(
                this,
                 if (isWorkSession) R.color.work_red else R.color.break_green
            )
        )
    }

    private fun saveTimerState() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit()
            .putLong("time_left", timeLeftMillis)
            .putBoolean("is_running", isRunning)
            .putBoolean("is_paused", isPaused)
            .putBoolean("is_work_session", isWorkSession)
            .putBoolean("is_long_break", isLongBreak)
            .putInt("completed_pomodoros", completedPomodoros)
            .apply()
    }

    private fun restoreTimerState(): Boolean {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        if (!prefs.contains("time_left")) return false

        timeLeftMillis = prefs.getLong("time_left", timeLeftMillis)
        isRunning = prefs.getBoolean("is_running", false)
        isPaused = prefs.getBoolean("is_paused", false)
        isWorkSession = prefs.getBoolean("is_work_session", true)
        isLongBreak = prefs.getBoolean("is_long_break", false)
        completedPomodoros = prefs.getInt("completed_pomodoros", 0)

        return true

    }

    private fun animateSessionChange() {
        tvSessionType.animate()
            .alpha(0f)
            .setDuration(150)
            .withEndAction {
                updateSessionLabel()
                tvSessionType.animate()
                    .alpha(1f)
                    .setDuration(150)
            }

        timerCard.animate()
            .scaleX(1.03f)
            .scaleY(1.03f)
            .setDuration(150)
            .withEndAction {
                timerCard.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
            }
    }


}





