package it.unimib.indoornav.lab.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import it.unimib.indoornav.lab.capture.LiveStreamCounter
import it.unimib.indoornav.lab.capture.SessionWriter
import it.unimib.indoornav.lab.data.SetupConfig
import it.unimib.indoornav.lab.format.ExperimentTemplate
import it.unimib.indoornav.lab.format.RouteCheckpoints
import it.unimib.indoornav.lab.format.RecordingEdits
import it.unimib.indoornav.lab.format.SessionEditor
import it.unimib.indoornav.lab.format.SessionHealthReport
import it.unimib.indoornav.lab.format.SessionManifest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class MarkHistoryItem(
    val kind: String,
    val label: String,
    val source: String,
)

/** A finished recording, shown to the user once so they can correct its details or delete it. */
data class RecordingSummary(
    val sessionDir: File,
    val template: ExperimentTemplate?,
    val manifest: SessionManifest,
    val health: SessionHealthReport,
    val checkpointMarks: Int,
)

data class RecordingUiState(
    val isRecording: Boolean = false,
    val recordingMode: String = "guided",
    val carryMode: String = "hand-up",
    val templateCode: String = "",
    val testId: String = "",
    val sessionFolderName: String = "",
    val elapsedSec: Long = 0L,
    val twentyMinuteWarning: Boolean = false,
    val protocolStatus: ProtocolStatus = ProtocolStatus(),
    val streamCounters: List<LiveStreamCounter> = emptyList(),
    val routeName: String = "",
    val nextCheckpointIndex: Int = 0,
    val lastMark: MarkHistoryItem? = null,
    val touchLocked: Boolean = false,
    val allowScreenOff: Boolean = false,
    val inventoryMode: Boolean = false,
    val pendingSummary: RecordingSummary? = null,
) {
    val nextCheckpointLabel: String
        get() = RouteCheckpoints.labelAt(routeName, nextCheckpointIndex)

    val routeComplete: Boolean
        get() = RouteCheckpoints.isComplete(routeName, nextCheckpointIndex)
}

class RecordingCoordinator(private val context: Context) {
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _uiState = MutableStateFlow(RecordingUiState())
    val uiState: StateFlow<RecordingUiState> = _uiState.asStateFlow()

    @Volatile
    internal var activeService: RecordingService? = null

    @Volatile
    internal var activeWriter: SessionWriter? = null

    fun emergencyFlushActiveWriter() {
        activeWriter?.emergencyFlush()
    }

    fun startRecording(config: SetupConfig) {
        val intent = Intent(context, RecordingService::class.java).apply {
            action = RecordingService.ACTION_START
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun requestFinish() {
        activeService?.requestFinish()
    }

    fun requestImmediateStop() {
        activeService?.requestImmediateStop()
    }

    fun markCheckpoint(source: String) {
        activeService?.recordCheckpointMark(source)
    }

    fun undoLastMark() {
        activeService?.undoLastMark()
    }

    fun markEvent(kind: String, label: String = "", source: String = "button") {
        activeService?.recordCustomMark(kind, label, source)
    }

    fun setTouchLocked(locked: Boolean) {
        _uiState.value = _uiState.value.copy(touchLocked = locked)
    }

    fun setAllowScreenOff(allow: Boolean) {
        _uiState.value = _uiState.value.copy(allowScreenOff = allow)
    }

    fun onAppForegroundChanged(inForeground: Boolean) {
        activeService?.logAppForegroundState(inForeground)
    }

    fun updateState(transform: (RecordingUiState) -> RecordingUiState) {
        _uiState.value = transform(_uiState.value)
    }

    /** Writes [edits] into the manifest and renames the folder if the test ID or phone label changed. */
    fun applySummaryEdits(edits: RecordingEdits, onDone: (String) -> Unit = {}) {
        val summary = _uiState.value.pendingSummary ?: return
        _uiState.value = _uiState.value.copy(pendingSummary = null)
        ioScope.launch {
            val msg = runCatching {
                val updated = SessionEditor.apply(summary.manifest, summary.template, edits)
                if (updated == summary.manifest) return@runCatching "Recording kept as it was"
                File(summary.sessionDir, "manifest.json").writeText(updated.toJsonString())
                val target = File(summary.sessionDir.parentFile, SessionEditor.folderNameFor(updated))
                when {
                    target == summary.sessionDir -> "Recording details updated"
                    target.exists() -> "Details updated; folder not renamed because ${target.name} already exists"
                    summary.sessionDir.renameTo(target) -> "Details updated; saved as ${target.name}"
                    else -> "Details updated; the folder could not be renamed"
                }
            }.getOrElse { "Could not update the recording: ${it.message}" }
            onDone(msg)
        }
    }

    fun deleteSummarySession(onDone: (String) -> Unit = {}) {
        val summary = _uiState.value.pendingSummary ?: return
        _uiState.value = _uiState.value.copy(pendingSummary = null)
        ioScope.launch {
            val ok = summary.sessionDir.deleteRecursively()
            onDone(if (ok) "Recording deleted" else "Could not delete ${summary.sessionDir.name}")
        }
    }

    fun dismissSummary() {
        _uiState.value = _uiState.value.copy(pendingSummary = null)
    }
}
