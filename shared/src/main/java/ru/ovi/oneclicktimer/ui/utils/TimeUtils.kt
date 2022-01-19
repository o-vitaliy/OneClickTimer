package ru.ovi.oneclicktimer.ui.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

private val minutesTimerFormat = SimpleDateFormat("mm 'min :' ss 'sec'")
private val secondsTimerFormat = SimpleDateFormat("ss 'sec'")


fun timerFormat(time: Long): String {
    val formatter = if (time >= TimeUnit.MINUTES.toMillis(1))
        minutesTimerFormat else secondsTimerFormat

    return formatter.format(Date(time))
}
