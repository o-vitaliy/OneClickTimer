package ru.ovi.oneclicktimer.data

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine

class TimerSelectedValuePrefs @Inject constructor(application: Application) {
    private val prefs = application.getSharedPreferences(javaClass.name, Context.MODE_PRIVATE)

    fun getCurrentValue(): Long? {
        return if (prefs.contains(CURRENT_VALUE)) prefs.getLong(CURRENT_VALUE, -1)
        else null
    }

    fun setCurrentValue(value: Long?) {
        prefs.edit {
            if (value == null) remove(CURRENT_VALUE)
            else putLong(CURRENT_VALUE, value)
        }
    }


    private companion object {
        const val CURRENT_VALUE = "CURRENT_VALUE"
    }
}
