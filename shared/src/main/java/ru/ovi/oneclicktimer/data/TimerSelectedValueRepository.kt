package ru.ovi.oneclicktimer.data

import javax.inject.Inject

class TimerSelectedValueRepository @Inject constructor(private val datasource: TimerSelectedValuePrefs) {
    fun getCurrentValue() = datasource.getCurrentValue()
    fun setCurrentValue(value: Long?) = datasource.setCurrentValue(value)
}
