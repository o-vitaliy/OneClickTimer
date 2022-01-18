package ru.ovi.oneclicktimer.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import com.google.accompanist.pager.ExperimentalPagerApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.InternalCoroutinesApi
import ru.ovi.oneclicktimer.ui.theme.OneClickTimerTheme
import ru.ovi.oneclicktimer.ui.timer.TimerWidget
import ru.ovi.oneclicktimer.ui.timerpicker.TimerPicker

@OptIn(ExperimentalPagerApi::class, InternalCoroutinesApi::class)
@AndroidEntryPoint
class TimerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OneClickTimerTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    TimerWidget()
                }
            }
        }
    }
}
