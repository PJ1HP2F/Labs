package com.example.timerapp.service

import android.app.*
import android.content.Intent
import android.media.RingtoneManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.*
import androidx.core.app.NotificationCompat
import com.example.timerapp.R
import com.example.timerapp.data.model.Phase
import com.example.timerapp.data.model.TimerSequence
import com.example.timerapp.ui.main.MainActivity
import com.google.gson.Gson
import kotlinx.coroutines.*

class TimerService : Service() {

    companion object {
        const val CHANNEL_ID = "timer_channel"
        const val NOTIFICATION_ID = 1

        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_NEXT = "ACTION_NEXT"
        const val ACTION_PREV = "ACTION_PREV"
        const val ACTION_STOP = "ACTION_STOP"

        const val EXTRA_SEQUENCE = "extra_sequence"

        // Broadcasts
        const val BROADCAST_TICK = "com.example.timerapp.TICK"
        const val EXTRA_PHASE_INDEX = "phase_index"
        const val EXTRA_REMAINING = "remaining"
        const val EXTRA_IS_RUNNING = "is_running"
        const val EXTRA_IS_FINISHED = "is_finished"

        var isRunning = false
    }

    private val binder = TimerBinder()
    private var phases: List<Phase> = emptyList()
    private var currentPhaseIndex = 0
    private var remainingSeconds = 0
    private var isPaused = false
    private var timerJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val sequenceJson = intent.getStringExtra(EXTRA_SEQUENCE) ?: return START_STICKY
                val sequence = Gson().fromJson(sequenceJson, TimerSequence::class.java)
                startTimer(sequence)
            }
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_NEXT -> nextPhase()
            ACTION_PREV -> prevPhase()
            ACTION_STOP -> stopSelf()
        }
        return START_STICKY
    }

    private fun startTimer(sequence: TimerSequence) {
        phases = sequence.buildPhases()
        currentPhaseIndex = 0
        remainingSeconds = phases.firstOrNull()?.durationSeconds ?: 0
        isPaused = false
        launchTicker()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    private fun launchTicker() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                if (isPaused) {
                    delay(200L)
                    continue
                }
                
                delay(1000L)
                
                if (isPaused || !isActive) continue

                remainingSeconds--
                if (remainingSeconds <= 0) {
                    val hasNext = advancePhase()
                    if (!hasNext) break
                } else {
                    broadcast()
                    updateNotification()
                }
            }
        }
    }

    private fun advancePhase(): Boolean {
        playBeep()
        return if (currentPhaseIndex < phases.size - 1) {
            currentPhaseIndex++
            remainingSeconds = phases[currentPhaseIndex].durationSeconds
            broadcast()
            updateNotification()
            true
        } else {
            // Finished
            remainingSeconds = 0
            broadcastFinished()
            stopSelf()
            false
        }
    }

    private fun nextPhase() {
        if (currentPhaseIndex < phases.size - 1) {
            playBeep()
            currentPhaseIndex++
            remainingSeconds = phases[currentPhaseIndex].durationSeconds
            broadcast()
            updateNotification()
        }
    }

    private fun prevPhase() {
        if (currentPhaseIndex > 0) {
            currentPhaseIndex--
            remainingSeconds = phases[currentPhaseIndex].durationSeconds
            broadcast()
            updateNotification()
        }
    }

    private fun pauseTimer() {
        isPaused = true
        broadcast()
        updateNotification()
    }

    private fun resumeTimer() {
        isPaused = false
        broadcast()
        updateNotification()
    }

    private fun playBeep() {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 400)
            Handler(Looper.getMainLooper()).postDelayed({
                toneGen.release()
            }, 600)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun broadcast() {
        val intent = Intent(BROADCAST_TICK).apply {
            setPackage(packageName)
            putExtra(EXTRA_PHASE_INDEX, currentPhaseIndex)
            putExtra(EXTRA_REMAINING, remainingSeconds.coerceAtLeast(0))
            putExtra(EXTRA_IS_RUNNING, !isPaused)
            putExtra(EXTRA_IS_FINISHED, false)
        }
        sendBroadcast(intent)
    }

    private fun broadcastFinished() {
        val intent = Intent(BROADCAST_TICK).apply {
            setPackage(packageName)
            putExtra(EXTRA_IS_FINISHED, true)
        }
        sendBroadcast(intent)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Timer Notifications",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows timer progress"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val phase = phases.getOrNull(currentPhaseIndex)
        val phaseName = phase?.type?.name ?: "Timer"
        val timeStr = formatTime(remainingSeconds)

        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, TimerService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE
        )

        val pauseResumeIntent = PendingIntent.getService(
            this, 2,
            Intent(this, TimerService::class.java).apply {
                action = if (isPaused) ACTION_RESUME else ACTION_PAUSE
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer)
            .setContentTitle(phaseName)
            .setContentText(timeStr)
            .setOngoing(true)
            .setContentIntent(openIntent)
            .addAction(
                R.drawable.ic_pause,
                if (isPaused) "Resume" else "Pause",
                pauseResumeIntent
            )
            .addAction(R.drawable.ic_stop, "Stop", stopIntent)
            .build()
    }

    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%02d:%02d".format(m, s)
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        timerJob?.cancel()
        serviceScope.cancel()
    }
}
