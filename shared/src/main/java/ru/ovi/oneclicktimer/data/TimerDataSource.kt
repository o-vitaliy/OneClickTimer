package ru.ovi.oneclicktimer.data

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.ovi.oneclicktimer.ui.service.TimerService
import java.lang.ref.WeakReference
import javax.inject.Inject
import kotlin.coroutines.resume

@ActivityRetainedScoped
class TimerDataSource @Inject constructor(private val activity: Application) {

    private var service: TimerService? = null
    private var binder: WeakReference<ServiceConnection>? = null

    fun isRunning() = service?.timerRunner != null

    suspend fun start(duration: Long): Unit = suspendCancellableCoroutine { continuation ->
        val serviceConnection: ServiceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                service = (binder as TimerService.RunServiceBinder?)?.service

                continuation.resume(Unit)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                service = null
                continuation.cancel()
            }
        }

        binder = WeakReference(serviceConnection)

        val intent = serviceIntent()
            .putExtra(TimerService.DURATION, duration)

        activity.startService(intent)
        activity.bindService(intent, serviceConnection, 0)
    }

    fun subscribe(): Flow<Long> {
        return service?.timerRunner?.secondsTick ?: emptyFlow()
    }

    fun stop() {
        binder?.get()?.let { activity.unbindService(it) }
        activity.stopService(serviceIntent())
        service = null
        binder = null
    }

    private fun serviceIntent() = Intent(activity, TimerService::class.java)

}
