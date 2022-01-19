package ru.ovi.oneclicktimer.ui.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.ovi.oneclicktimer.domain.usecase.currentTimerValue.GetCurrentTimerValueUseCase
import ru.ovi.oneclicktimer.domain.usecase.currentTimerValue.SetCurrentTimerValueUseCase
import ru.ovi.oneclicktimer.domain.usecase.timer.IsTimerRunningUseCase
import ru.ovi.oneclicktimer.domain.usecase.timer.StartTimerRunningUseCase
import ru.ovi.oneclicktimer.domain.usecase.timer.StopTimerRunningUseCase
import ru.ovi.oneclicktimer.domain.usecase.timer.SubscribeTimerUseCase
import ru.ovi.oneclicktimer.ui.timerpicker.TIME_SLOTS
import ru.ovi.oneclicktimer.ui.timerpicker.TIME_SLOTS_DEFAULT
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val isTimerRunningUseCase: IsTimerRunningUseCase,
    private val startTimerRunningUseCase: StartTimerRunningUseCase,
    private val stopTimerRunningUseCase: StopTimerRunningUseCase,
    private val subscribeTimerUseCase: SubscribeTimerUseCase,
    private val setCurrentTimerValueUseCase: SetCurrentTimerValueUseCase,
    private val getCurrentTimerValueUseCase: GetCurrentTimerValueUseCase,
) : ViewModel() {

    private val _timerState = MutableStateFlow(TimerState.IDLE)
    val timerState = _timerState.asStateFlow()

    private val _timerDuration = MutableStateFlow<Long?>(null)
    val timerDuration = _timerDuration.asStateFlow()

    private val _ticks = MutableSharedFlow<Long>(1, 1, BufferOverflow.DROP_OLDEST)
    val ticks = _ticks.asSharedFlow()

    init {
        viewModelScope.launch {
            if (isTimerRunningUseCase.invoke(Unit).getOrNull() == true) {
                setTimerRunning()
            }

            val initialTimerValue = getCurrentTimerValueUseCase.invoke(Unit).getOrNull()
                ?: TIME_SLOTS_DEFAULT

            _timerDuration.emit(initialTimerValue)
            Timber.d("timer loaded")
        }
    }

    fun timerChanged(time: Long) {
        assert(TIME_SLOTS.contains(time))
        viewModelScope.launch {
            _timerDuration.emit(time)
            setCurrentTimerValueUseCase.invoke(time)
            Timber.d("timer changed")
        }
    }

    fun startTimer() {
        if (_timerState.value == TimerState.RUNNING) return

        viewModelScope.launch {
            startTimerRunningUseCase.invoke(timerDuration.value ?: TIME_SLOTS_DEFAULT)
            setTimerRunning()
        }
    }

    private fun setTimerRunning() {
        viewModelScope.launch {
            _timerState.emit(TimerState.RUNNING)
            _ticks.emitAll(subscribeTimerUseCase.execute())
            _timerState.emit(TimerState.IDLE)
        }
    }

    fun stopTimer() {
        if (_timerState.value != TimerState.RUNNING) return

        viewModelScope.launch {
            stopTimerRunningUseCase.invoke(Unit)
        }
    }
}
