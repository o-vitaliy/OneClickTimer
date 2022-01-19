package ru.ovi.oneclicktimer.ui.service

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

class TimerRunner(private val duration: Long) {

    private val startTime: Long = System.currentTimeMillis()

    private val _secondsTick = MutableSharedFlow<State>(1, 1, BufferOverflow.DROP_OLDEST)
    val secondsTick: Flow<Long> = _secondsTick
        .takeWhile { it is State.Running }
        .map { it as State.Running }
        .map { it.duration }
        .distinctUntilChanged { old, new -> old / SECOND == new / SECOND }

    private val timerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    init {
        runTimer()
    }

    private fun runTimer() {
        timerScope.launch {
            var delta = System.currentTimeMillis() - startTime
            _secondsTick.emit(State.Running(duration))
            while (duration - delta > 0) {
                _secondsTick.emit(State.Running(duration - delta))
                delta = System.currentTimeMillis() - startTime
                delay(100)
            }
            _secondsTick.emit(State.Running(0))
            _secondsTick.emit(State.Finished)
        }
    }

    fun cancel() {
        if (!timerScope.isActive) return
        timerScope.launch {
            _secondsTick.emit(State.Finished)
        }
        timerScope.cancel()
    }


    protected fun finalize() {
        cancel()
    }

    private sealed interface State {
        class Running(val duration: Long) : State
        object Finished : State

    }

    private companion object {
        const val SECOND = 1_000
    }

}
