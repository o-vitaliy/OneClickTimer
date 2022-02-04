package ru.ovi.oneclicktimer.ui.timer

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.InternalCoroutinesApi
import ru.ovi.oneclicktimer.ui.timerpicker.TimerPicker
import ru.ovi.oneclicktimer.ui.utils.timerFormat

@ExperimentalPagerApi
@InternalCoroutinesApi
@Composable
fun TimerWidget(
    viewModel: TimerViewModel = viewModel()
) {
    val timerState by viewModel.timerState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (timerState == TimerState.IDLE) {
            val selectedTimerDuration by viewModel.timerDuration.collectAsState()
            selectedTimerDuration?.let {
                TimerPicker(it, viewModel::timerChanged)
            }
        } else {
            val tick by viewModel.ticks.collectAsState(initial = 0)
            Text(
                modifier = Modifier
                    .height(56.dp)
                    .wrapContentHeight(Alignment.CenterVertically) ,
                text = timerFormat(tick)
            )
        }
        TimerButton(timerState == TimerState.RUNNING, viewModel::startTimer, viewModel::stopTimer)
    }
}

@Composable
private fun TimerButton(isRunning: Boolean, onStart: () -> Unit, onStop: () -> Unit) {
    Button(onClick = {
        if (isRunning) onStop() else onStart()
    }) {
        Text(text = if (isRunning) "Stop" else "Start")
    }
}
