package ru.ovi.oneclicktimer.ui.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.ovi.oneclicktimer.domain.usecase.timer.IsTimerRunningUseCase
import ru.ovi.oneclicktimer.domain.usecase.timer.StartTimerRunningUseCase
import ru.ovi.oneclicktimer.domain.usecase.timer.StopTimerRunningUseCase
import ru.ovi.oneclicktimer.domain.usecase.timer.SubscribeTimerUseCase
import ru.ovi.oneclicktimer.ui.timerpicker.TIME_SLOTS
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val isTimerRunningUseCase: IsTimerRunningUseCase,
    private val startTimerRunningUseCase: StartTimerRunningUseCase,
    private val stopTimerRunningUseCase: StopTimerRunningUseCase,
    private val subscribeTimerUseCase: SubscribeTimerUseCase,
) : ViewModel() {

    private val _timerState = MutableStateFlow(TimerState.IDLE)
    val timerState = _timerState.asStateFlow()

    private var timerDuration = TIME_SLOTS.first()
    var timerPickerIndex: Int = 0

    private val _ticks = MutableSharedFlow<Long>(1, 1, BufferOverflow.DROP_OLDEST)
    val ticks = _ticks.asSharedFlow()

    init {
        viewModelScope.launch {
            if (isTimerRunningUseCase.invoke(Unit).getOrNull() == true) {
                setTimerRunning()
            }
        }
    }

    fun timerChanged(index: Int, time: Long) {
        assert(TIME_SLOTS.contains(time))
        timerPickerIndex = index
        timerDuration = time
    }

    fun startTimer() {
        if (_timerState.value == TimerState.RUNNING) return

        viewModelScope.launch {
            startTimerRunningUseCase.invoke(timerDuration)
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
