package ru.ovi.oneclicktimer

import android.app.Activity
import android.os.Bundle
import ru.ovi.oneclicktimer.databinding.ActivityWatchTimerBinding

class WatchTimerActivity : Activity() {

    private lateinit var binding: ActivityWatchTimerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWatchTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}
