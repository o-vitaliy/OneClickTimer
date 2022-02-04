package ru.ovi.oneclicktimer

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.fragment.app.FragmentActivity
import androidx.wear.ambient.AmbientModeSupport
import com.google.accompanist.pager.ExperimentalPagerApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.InternalCoroutinesApi
import ru.ovi.oneclicktimer.ui.timer.TimerWidget

@OptIn(ExperimentalPagerApi::class, InternalCoroutinesApi::class)
@AndroidEntryPoint
class WatchTimerActivity : FragmentActivity(), AmbientModeSupport.AmbientCallbackProvider {
    private lateinit var ambientController: AmbientModeSupport.AmbientController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ambientController = AmbientModeSupport.attach(this)
        setContent {
            // A surface container using the 'background' color from the theme
            Surface(color = MaterialTheme.colors.background) {
                TimerWidget()

            }
        }

    }

    override fun getAmbientCallback(): AmbientModeSupport.AmbientCallback {
        return object : AmbientModeSupport.AmbientCallback() {

        }
    }
}
