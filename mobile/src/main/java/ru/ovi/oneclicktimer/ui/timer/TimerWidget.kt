package ru.ovi.oneclicktimer.ui.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.platform.LocalContext
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
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (timerState == TimerState.IDLE) {
            TimerPicker(viewModel.timerPickerIndex, viewModel::timerChanged)
        } else {
            val tick by viewModel.ticks.collectAsState(initial = 0)
            Text(text = timerFormat(tick))
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
