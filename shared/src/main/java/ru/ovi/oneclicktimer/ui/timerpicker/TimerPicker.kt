package ru.ovi.oneclicktimer.ui.timerpicker

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.calculateCurrentOffsetForPage
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import ru.ovi.oneclicktimer.ui.utils.timerFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue

val TIME_SLOTS = arrayOf(
    TimeUnit.SECONDS.toMillis(30),
    TimeUnit.SECONDS.toMillis(45),
    TimeUnit.SECONDS.toMillis(60),
    TimeUnit.SECONDS.toMillis(90),
    TimeUnit.SECONDS.toMillis(120),
)

val TIME_SLOTS_DEFAULT = TIME_SLOTS.first()

@InternalCoroutinesApi
@ExperimentalPagerApi
@Composable
fun TimerPicker(
    currentValue: Long = TIME_SLOTS_DEFAULT,
    onSelected: ((Long) -> Unit)? = null
) {
    val pagerState = rememberPagerState(initialPage = TIME_SLOTS.indexOf(currentValue))
    val paddingValues = getPaddingValues(LocalConfiguration.current)
    HorizontalPager(
        count = TIME_SLOTS.size, state = pagerState,
        contentPadding = PaddingValues(horizontal = paddingValues.dp),
    ) { page ->
        TimerItem(TIME_SLOTS[page], calculateCurrentOffsetForPage(page).absoluteValue)
    }

    LaunchedEffect(pagerState) {
        // Collect from the a snapshotFlow reading the currentPage
        snapshotFlow { pagerState.currentPage }.collect { page ->
            onSelected?.invoke(TIME_SLOTS[page])
        }
    }
}

private fun getPaddingValues(configuration: Configuration): Int {
    val screenWidth = configuration.screenWidthDp
    return (screenWidth * 0.25).toInt()
}

@InternalCoroutinesApi
@ExperimentalPagerApi
@Composable
@Preview(widthDp = 320)
fun TimerPickerPreviewWide() {
    TimerPicker()
}

@InternalCoroutinesApi
@ExperimentalPagerApi
@Composable
@Preview(widthDp = 240)
fun TimerPickerPreviewThin() {
    TimerPicker()
}


@Composable
fun TimerItem(timerSlot: Long, pageOffset: Float) {

    val color = remember { randomColor() }
    Card(
        backgroundColor = color,
        modifier = Modifier
            .graphicsLayer {
                // Calculate the absolute offset for the current page from the
                // scroll position. We use the absolute value which allows us to mirror
                // any effects for both directions

                // We animate the scaleX + scaleY, between 85% and 100%
                lerp(
                    start = 0.85f,
                    stop = 1f,
                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                ).also { scale ->
                    scaleX = scale
                    scaleY = scale
                }

                // We animate the alpha, between 50% and 100%
                alpha = lerp(
                    start = 0.5f,
                    stop = 1f,
                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                )
            }
            .height(60.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = timerFormat(timerSlot),
                modifier = Modifier.wrapContentSize()
            )
        }
    }
}


@Composable
@Preview
fun TimerItemPreview90() {
    TimerItem(TimeUnit.SECONDS.toMillis(90), 0f)
}

@Composable
@Preview
fun TimerItemPreview30() {
    TimerItem(TimeUnit.SECONDS.toMillis(30), 0f)
}


private fun randomColor(): Color {
    val r = Random()
    return Color(
        r.nextInt(256),
        r.nextInt(256),
        r.nextInt(256),
        255,
    )
}
