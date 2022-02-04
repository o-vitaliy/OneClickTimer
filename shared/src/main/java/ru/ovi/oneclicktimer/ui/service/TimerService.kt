package ru.ovi.oneclicktimer.ui.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import ru.ovi.oneclicktimer.ui.timerpicker.TIME_SLOTS
import ru.ovi.oneclicktimer.ui.utils.timerFormat
import ru.ovi.shared.R

class TimerService : Service() {

    var timerRunner: TimerRunner? = null

    private var duration = DEFAULT_TIMER

    // Service binder
    private val serviceBinder: IBinder = RunServiceBinder(this)

    private val timerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    class RunServiceBinder(val service: TimerService) : Binder()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when {
            intent?.hasExtra(ACTION_CANCEL) == true -> {
                stopForeground(true)
                stopSelf()
            }
            intent?.hasExtra(ACTION_STOP) == true -> {
                stopTimer()
                createNotification(false, duration)
            }
            else -> {
                if (intent?.hasExtra(DURATION) == true) {
                    duration = intent.getLongExtra(DURATION, DEFAULT_TIMER)
                }
                startTimer()
                startForeground(NOTIFICATION_ID, createNotification(true, duration))
            }
        }

        return START_NOT_STICKY
    }

    private fun startTimer() {
        timerRunner = TimerRunner(duration).also {
            timerScope.launch {
                it.secondsTick.collect {
                    notificationManager.notify(NOTIFICATION_ID, createNotification(true, it))
                }
                vibrateDevice()
                notificationManager.notify(NOTIFICATION_ID, createNotification(false, duration))
            }
        }
    }

    private val notificationManager
        get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private fun stopTimer() {
        timerRunner?.cancel()
        timerRunner = null
    }

    override fun onBind(intent: Intent?): IBinder {
        return serviceBinder
    }

    override fun onDestroy() {
        timerScope.cancel()
        stopTimer()
        super.onDestroy()
    }

    private fun createNotification(running: Boolean, duration: Long): Notification {
        val remoteView = RemoteViews(packageName, R.layout.notification_timer_control)

        remoteView.setOnClickPendingIntent(R.id.cancelButton, cancelIntent())

        if (running) {
            remoteView.setTextViewText(
                R.id.timerTitle,
                "Timer is running: ${timerFormat(duration)}"
            )

            remoteView.setTextViewText(R.id.actionButton, "Stop")
            remoteView.setTextViewCompoundDrawablesRelative(
                R.id.actionButton,
                R.drawable.ic_baseline_stop_circle_24, 0, 0, 0
            )

            remoteView.setOnClickPendingIntent(R.id.actionButton, stopIntent())

            remoteView.setViewVisibility(R.id.actionButtonPrev, View.INVISIBLE)
            remoteView.setViewVisibility(R.id.actionButtonNext, View.INVISIBLE)
        } else {
            remoteView.setTextViewText(R.id.timerTitle, "Time expired")

            remoteView.setViewVisibility(R.id.actionButtonPrev, View.VISIBLE)
            remoteView.setViewVisibility(R.id.actionButtonNext, View.VISIBLE)

            remoteView.setTextViewText(R.id.actionButton, "Start ${timerFormat(duration)}")
            remoteView.setTextViewCompoundDrawablesRelative(
                R.id.actionButton,
                R.drawable.ic_baseline_play_circle_filled_24, 0, 0, 0
            )

            remoteView.setOnClickPendingIntent(R.id.actionButton, startIntent(duration))

            val index = TIME_SLOTS.indexOf(duration)
            if (index == 0) {
                remoteView.setBoolean(R.id.actionButtonPrev, "setEnabled", false)
            } else {
                remoteView.setBoolean(R.id.actionButtonPrev, "setEnabled", true)
                val prevDuration = TIME_SLOTS[index - 1]
                remoteView.setTextViewText(R.id.actionButtonPrev, timerFormat(prevDuration))
                remoteView.setOnClickPendingIntent(R.id.actionButtonPrev, startIntent(prevDuration))
            }

            if (index == TIME_SLOTS.size - 1) {
                remoteView.setBoolean(R.id.actionButtonNext, "setEnabled", false)
            } else {
                remoteView.setBoolean(R.id.actionButtonNext, "setEnabled", true)
                val nextDuration = TIME_SLOTS[index + 1]
                remoteView.setTextViewText(R.id.actionButtonNext, timerFormat(nextDuration))
                remoteView.setOnClickPendingIntent(R.id.actionButtonNext, startIntent(nextDuration))
            }
        }


        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        } else {
            ""
        }

        val timerIcon =
            if (running) R.drawable.ic_baseline_timer_24
            else R.drawable.ic_baseline_timer_off_24

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(timerIcon)
            .setContent(remoteView)
            .setOngoing(true)

        val resultIntent = Intent(Intent.ACTION_MAIN)
            .setPackage(packageName)
        val resultPendingIntent = PendingIntent.getActivity(
            this, 0, resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        builder.setContentIntent(resultPendingIntent)
        return builder.build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String {
        val channelId = "oneckicktimer"
        val channel = NotificationChannel(
            channelId, "timer_service",
            NotificationManager.IMPORTANCE_LOW,
        )
        notificationManager.createNotificationChannel(channel)
        return channelId
    }

    private fun cancelIntent() = PendingIntent.getService(
        this, 1,
        Intent(this, TimerService::class.java).putExtra(ACTION_CANCEL, true),
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    private fun stopIntent() = PendingIntent.getService(
        this, 2,
        Intent(this, TimerService::class.java).putExtra(ACTION_STOP, true),
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    private fun startIntent(duration: Long) = PendingIntent.getService(
        this, 3 + duration.toInt(),
        Intent(this, TimerService::class.java)
            .putExtra(ACTION_START, true)
            .putExtra(DURATION, duration),
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    private fun vibrateDevice() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= 26) {
                it.vibrate(
                    VibrationEffect.createOneShot(
                        VIBRATION_DURATION,
                        255
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(VIBRATION_DURATION)
            }
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 100
        const val DURATION = "duration"

        private const val DEFAULT_TIMER = 60L

        private const val ACTION_STOP = "ACTION_STOP"
        private const val ACTION_START = "ACTION_START"
        private const val ACTION_CANCEL = "ACTION_CANCEL"

        private const val VIBRATION_DURATION = 300L
    }
}
