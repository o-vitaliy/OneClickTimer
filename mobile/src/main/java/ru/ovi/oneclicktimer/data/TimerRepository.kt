package ru.ovi.oneclicktimer.data

import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

@ActivityRetainedScoped
class TimerRepository @Inject constructor(private val dataSource: TimerDataSource) {

    suspend fun start(duration: Long) = dataSource.start(duration)

    fun subscribe() = dataSource.subscribe()

    fun stop() = dataSource.stop()

    fun isRunning(): Boolean = dataSource.isRunning()
}
