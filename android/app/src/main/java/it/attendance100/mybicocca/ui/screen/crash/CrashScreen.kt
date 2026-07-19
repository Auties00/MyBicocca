package it.attendance100.mybicocca.ui.screen.crash

import android.widget.Toast
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.crash.CrashLogBuilder
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Last-resort screen shown after an uncaught exception killed the main process. Deliberately
 * self-contained: no ViewModel, no Hilt, no navigation — it runs in the `:crash` process where
 * none of that machinery is up, and the less it depends on, the less can fail here. Shows the
 * truncated stack trace in a selectable monospace box between a "save crash log" action (share
 * sheet via [CrashLogBuilder]) and the restart button wired by the host activity.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalAnimationGraphicsApi::class)
@Composable
fun CrashScreen(
    stackTrace: String?,
    onRestart: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val crashLogBuilder = remember { CrashLogBuilder(context) }
    var dumping by remember { mutableStateOf(false) }
    val saveFailedMessage = stringResource(R.string.crash_screen_save_failed)

    // Picked once per crash (remember survives recomposition, and the process is fresh each
    // time the screen appears), so the title doesn't reshuffle while the user is looking at it.
    val titles = stringArrayResource(R.array.crash_screen_titles)
    val title = remember { titles.random() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Spacer(Modifier.height(64.dp))
        // Wiggles on entry, rests ~7 s, wiggles again, forever. The animated-vector painter
        // only plays start→end once, so each run recreates it via key(): the fresh painter
        // starts at rest and the LaunchedEffect flips atEnd to drive the wiggle.
        var wiggleRun by remember { mutableIntStateOf(0) }
        LaunchedEffect(Unit) {
            while (true) {
                delay(WIGGLE_PERIOD_MS.milliseconds)
                wiggleRun++
            }
        }
        key(wiggleRun) {
            val bug = AnimatedImageVector.animatedVectorResource(R.drawable.avd_bug_wiggle)
            var atEnd by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { atEnd = true }
            Icon(
                painter = rememberAnimatedVectorPainter(bug, atEnd),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmallEmphasized,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            // headlineSmall-sized by default, shrinking only when a long translation would otherwise not fit the width on a single line
            autoSize = TextAutoSize.StepBased(
                minFontSize = 14.sp,
                maxFontSize = MaterialTheme.typography.headlineSmallEmphasized.fontSize,
                stepSize = 0.25.sp,
            ),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(
                R.string.crash_screen_description,
                stringResource(R.string.app_name)
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Start,
        )
        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState()),
        ) {
            SelectionContainer {
                Text(
                    text = stackTrace ?: stringResource(R.string.crash_screen_no_stack_trace),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    softWrap = false,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (!dumping) {
                    dumping = true
                    scope.launch {
                        crashLogBuilder.dumpAndShare(stackTrace).onFailure {
                            Toast.makeText(context, saveFailedMessage, Toast.LENGTH_SHORT).show()
                        }
                        dumping = false
                    }
                }
            },
            enabled = !dumping,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.crash_screen_save_log))
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onRestart,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.crash_screen_restart))
        }
        Spacer(Modifier.height(16.dp))
    }
}

/** ~1.6 s of wiggle (the AVD's own length) + ~7 s of rest between runs. */
private const val WIGGLE_PERIOD_MS = 8_600L

private const val mockStackTrace = $$"""
                java.lang.IllegalArgumentException: NavDisplay backstack cannot be empty
                at lq.r(SourceFile:1)
                at x4d.c(SourceFile:486)
                at ht6.invoke(SourceFile:1662)
                at lz1.a(SourceFile:43)
                at lz1.invoke(SourceFile:9)
                at voc.a(SourceFile:186)
                at ft6.f(SourceFile:207)
                at lz1.h(SourceFile:43)
                at oe.invoke(SourceFile:877)
                at iw4.k0(SourceFile:594)
                at iw4.v(SourceFile:44)
                at zz9.a(SourceFile:72)
                at et6.f(SourceFile:362)
                at lz1.h(SourceFile:43)
                at oe.invoke(SourceFile:877)
                at iw4.k0(SourceFile:594)
                at iw4.s0(SourceFile:157)
                at iw4.R(SourceFile:131)
                at w12.y(SourceFile:109)
                at androidx.compose.runtime.Recomposer.M(SourceFile:94)
                at gt6.c(SourceFile:628)
                at nt.doFrame(SourceFile:7)
                at mt.doFrame(SourceFile:48)
                at android.view.Choreographer$CallbackRecord.run(Choreographer.java:1959)
                at android.view.Choreographer$CallbackRecord.run(Choreographer.java:1970)
                at android.view.Choreographer.doCallbacks(Choreographer.java:1423)
                at android.view.Choreographer.doFrame(Choreographer.java:1338)
                at android.view.Choreographer$FrameDisplayEventReceiver.run(Choreographer.java:1930)
                at android.os.Handler.handleCallback(Handler.java:1070)
                at android.os.Handler.dispatchMessage(Handler.java:125)
                at android.os.Looper.dispatchMessage(Looper.java:358)
                at android.os.Looper.loopOnce(Looper.java:288)
                at android.os.Looper.loop(Looper.java:392)
                at android.app.ActivityThread.main(ActivityThread.java:10346)
                at java.lang.reflect.Method.invoke(Native Method)
                at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:638)
                at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:972)
                Suppressed: j43: [ot@b23306f, ae7@b97a17c, wba{Cancelling}@4abba05, AndroidUiDispatcher@4a9ea5a]
            """

@Preview(name = "Crash Screen · Light", showBackground = true)
@Composable
private fun CrashScreenPreview() {
    BicoccaTheme(dark = false) {
        CrashScreen(
            stackTrace = mockStackTrace.trimIndent(),
            onRestart = {}
        )
    }
}

@Preview(name = "Crash Screen · Dark", showBackground = true)
@Composable
private fun CrashScreenDarkPreview() {
    BicoccaTheme(dark = true) {
        CrashScreen(
            stackTrace = mockStackTrace.trimIndent(),
            onRestart = {}
        )
    }
}
