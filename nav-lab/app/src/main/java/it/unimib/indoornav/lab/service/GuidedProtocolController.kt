package it.unimib.indoornav.lab.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class ProtocolPhase(val label: String) {
    IDLE("Idle"),
    START_SHAKE("Shake now"),
    PLACE_PHONE("Place phone"),
    START_STILL("Stand still"),
    ACTIVE("Recording"),
    FREE_AWAY_COUNTDOWN("Walk away countdown"),
    END_STILL("Stand still (end)"),
    END_SHAKE("Take phone out & shake"),
    DONE("Stopped"),
}

data class ProtocolStatus(
    val phase: ProtocolPhase = ProtocolPhase.IDLE,
    val countdownSec: Int = 0,
    val message: String = "",
)

class GuidedProtocolController(
    private val scope: CoroutineScope,
    private val prompter: VoicePrompter,
    private val logPhase: (String) -> Unit,
    private val onStopSession: () -> Unit,
) {
    private val _status = MutableStateFlow(ProtocolStatus())
    val status: StateFlow<ProtocolStatus> = _status.asStateFlow()

    private var activeJob: Job? = null
    private val shakeLock = Any()

    @Volatile
    private var waitingForShake = false

    @Volatile
    private var shakeArmed = false

    @Volatile
    private var spikeCount = 0

    @Volatile
    private var lastSpikeMs = 0L

    fun startGuided() {
        activeJob?.cancel()
        activeJob = scope.launch {
            prompter.awaitReady()
            prompter.beep()
            prompter.speak("Shake now.")
            logPhase("shake")
            waitForShakeOrTimeout(
                phase = ProtocolPhase.START_SHAKE,
                message = "Shake phone now",
                timeoutSec = 10,
                waitForSpeechBeforeArm = false,
                baseGraceMs = 600L,
            )

            prompter.speak("Place the phone.")
            logPhase("place")
            countdown(ProtocolPhase.PLACE_PHONE, "Place phone", seconds = 5, beepEachSecond = true)

            prompter.speak("Stand still.")
            logPhase("still")
            countdown(ProtocolPhase.START_STILL, "Stand still", seconds = 10, beepEachSecond = false)

            prompter.doubleBeep()
            prompter.speak("Go.")
            logPhase("go")
            _status.value = ProtocolStatus(ProtocolPhase.ACTIVE, 0, "Recording in progress")
        }
    }

    fun startFree(isStandMode: Boolean) {
        activeJob?.cancel()
        if (!isStandMode) {
            _status.value = ProtocolStatus(ProtocolPhase.ACTIVE, 0, "Free recording")
            return
        }
        activeJob = scope.launch {
            countdown(ProtocolPhase.FREE_AWAY_COUNTDOWN, "Walk away", seconds = 10, beepEachSecond = true)
            prompter.beep()
            logPhase("away")
            _status.value = ProtocolStatus(ProtocolPhase.ACTIVE, 0, "Free recording (stand)")
        }
    }

    fun startFollower() {
        activeJob?.cancel()
        _status.value = ProtocolStatus(ProtocolPhase.ACTIVE, 0, "Follower recording (touch locked)")
    }

    fun finishGuided() {
        val current = _status.value.phase
        if (current == ProtocolPhase.END_STILL || current == ProtocolPhase.END_SHAKE || current == ProtocolPhase.DONE) {
            return
        }
        activeJob?.cancel()
        activeJob = scope.launch {
            logPhase("finish")
            prompter.speak("Stand still.")
            logPhase("still_end")
            countdown(ProtocolPhase.END_STILL, "Stand still", seconds = 10, beepEachSecond = false)

            prompter.beep()
            prompter.speak("Take the phone out, shake now.")
            logPhase("shake_end")
            waitForShakeOrTimeout(
                phase = ProtocolPhase.END_SHAKE,
                message = "Take phone out and shake",
                timeoutSec = 15,
                waitForSpeechBeforeArm = true,
                baseGraceMs = 2_000L,
            )

            prompter.doubleBeep()
            logPhase("stop")
            _status.value = ProtocolStatus(ProtocolPhase.DONE, 0, "Stopped")
            onStopSession()
        }
    }

    fun finishImmediate(isFreeStand: Boolean) {
        activeJob?.cancel()
        if (isFreeStand) {
            logPhase("back")
        }
        logPhase("stop")
        _status.value = ProtocolStatus(ProtocolPhase.DONE, 0, "Stopped")
        onStopSession()
    }

    fun onAccelerometerSample(norm: Double) {
        if (!waitingForShake || !shakeArmed) return
        val now = System.currentTimeMillis()
        val dynamic = abs(norm - 9.81)
        if (dynamic <= 9.5) return
        synchronized(shakeLock) {
            if (!waitingForShake || !shakeArmed) return
            if (now - lastSpikeMs > 120L) {
                lastSpikeMs = now
                spikeCount++
                if (spikeCount >= 3) {
                    waitingForShake = false
                    shakeArmed = false
                }
            }
        }
    }

    fun cancel() {
        synchronized(shakeLock) {
            waitingForShake = false
            shakeArmed = false
        }
        activeJob?.cancel()
        activeJob = null
    }

    private suspend fun waitForShakeOrTimeout(
        phase: ProtocolPhase,
        message: String,
        timeoutSec: Int,
        waitForSpeechBeforeArm: Boolean,
        baseGraceMs: Long,
    ) {
        synchronized(shakeLock) {
            spikeCount = 0
            lastSpikeMs = 0L
            shakeArmed = false
            waitingForShake = true
        }

        val startMs = System.currentTimeMillis()
        val deadlineMs = startMs + timeoutSec * 1_000L
        val armJob = scope.launch {
            if (waitForSpeechBeforeArm) {
                prompter.awaitLastUtterance(maxWaitMs = 4_000L)
            }
            delay(baseGraceMs)
            synchronized(shakeLock) {
                if (waitingForShake) {
                    shakeArmed = true
                }
            }
        }

        var detected = false
        try {
            while (true) {
                val stillWaiting = synchronized(shakeLock) { waitingForShake }
                if (!stillWaiting) {
                    detected = true
                    break
                }
                val now = System.currentTimeMillis()
                val remainingMs = deadlineMs - now
                if (remainingMs <= 0L) break
                val remaining = ((remainingMs + 999L) / 1_000L).toInt().coerceAtLeast(1)
                _status.value = ProtocolStatus(phase, remaining, "$message (${remaining}s)")
                delay(150L)
            }
        } finally {
            armJob.cancel()
            synchronized(shakeLock) {
                waitingForShake = false
                shakeArmed = false
            }
        }

        if (detected) {
            // Preserve the 3rd spike peak and settling tail in acc.csv for sync.
            delay(1_000L)
        }
    }

    private suspend fun countdown(
        phase: ProtocolPhase,
        message: String,
        seconds: Int,
        beepEachSecond: Boolean,
    ) {
        for (sec in seconds downTo 1) {
            _status.value = ProtocolStatus(phase, sec, "$message (${sec}s)")
            if (beepEachSecond) {
                prompter.tickBeep()
            }
            delay(1_000L)
        }
    }
}
