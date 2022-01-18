package ru.ovi.oneclicktimer.ui.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import ru.ovi.oneclicktimer.R
import ru.ovi.oneclicktimer.ui.TimerActivity
import ru.ovi.oneclicktimer.ui.utils.timerFormat

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
                createNotification()
            }
            else -> {
                if (intent?.hasExtra(DURATION) == true) {
                    duration = intent.getLongExtra(DURATION, DEFAULT_TIMER)
                }
                startTimer()
                startForeground(NOTIFICATION_ID, createNotification(duration))
            }
        }

        return START_NOT_STICKY
    }

    private fun startTimer() {
        timerRunner = TimerRunner(duration).also {
            timerScope.launch {
                it.secondsTick.collect {
                    notificationManager.notify(NOTIFICATION_ID, createNotification(it))
                }
                vibrateDevice()
                notificationManager.notify(NOTIFICATION_ID, createNotification())
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

    private fun createNotification(duration: Long? = null): Notification {
        val remoteView = RemoteViews(packageName, R.layout.notification_timer_control)

        remoteView.setOnClickPendingIntent(R.id.cancelButton, cancelIntent())

        if (duration != null) {
            remoteView.setTextViewText(
                R.id.timerTitle,
                "Timer is running: ${timerFormat(duration)}"
            )

            remoteView.setTextViewText(R.id.actionButtonTitle, "Stop")
            remoteView.setImageViewResource(
                R.id.actionButtonImage,
                R.drawable.ic_baseline_stop_circle_24
            )

            remoteView.setOnClickPendingIntent(R.id.actionButton, stopIntent())
        } else {
            remoteView.setTextViewText(R.id.timerTitle, "Time expired")

            remoteView.setTextViewText(R.id.actionButtonTitle, "Start")
            remoteView.setImageViewResource(
                R.id.actionButtonImage,
                R.drawable.ic_baseline_play_circle_filled_24
            )

            remoteView.setOnClickPendingIntent(R.id.actionButton, startIntent())
        }


        val channelId = createNotificationChannel()

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_baseline_timer_24)
            .setContent(remoteView)

        val resultIntent = Intent(this, TimerActivity::class.java)
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
            NotificationManager.IMPORTANCE_DEFAULT,
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

    private fun startIntent() = PendingIntent.getService(
        this, 3,
        Intent(this, TimerService::class.java).putExtra(ACTION_START, true),
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    private fun vibrateDevice() {
        val vibrator = getSystemService( Vibrator::class.java)
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= 26) {
                it.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(100)
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
    }
}
