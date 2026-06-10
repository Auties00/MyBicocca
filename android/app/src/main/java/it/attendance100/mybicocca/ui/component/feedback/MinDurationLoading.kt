package it.attendance100.mybicocca.ui.component.feedback

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/**
 * Holds a loading flag visible for at least [minDurationMillis] once it has been shown, so a
 * fast fetch doesn't flash the indicator for a frame or two. If the data is already there on
 * first composition (loading == false from the start) nothing is ever held — content renders
 * immediately.
 */
@Composable
fun rememberMinDurationLoading(loading: Boolean, minDurationMillis: Long = 600L): Boolean {
    var visible by remember { mutableStateOf(loading) }
    var shownAtMillis by remember { mutableLongStateOf(if (loading) SystemClock.uptimeMillis() else 0L) }
    LaunchedEffect(loading) {
        if (loading) {
            if (!visible) {
                shownAtMillis = SystemClock.uptimeMillis()
                visible = true
            }
        } else if (visible) {
            val remainingMillis = minDurationMillis - (SystemClock.uptimeMillis() - shownAtMillis)
            if (remainingMillis > 0) delay(remainingMillis)
            visible = false
        }
    }
    return visible
}
