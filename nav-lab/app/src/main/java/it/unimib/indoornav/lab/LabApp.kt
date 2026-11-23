package it.unimib.indoornav.lab

import it.unimib.indoornav.lab.ui.map.MapCameraStore
import it.unimib.indoornav.lab.ui.map.MapFocusRequest
import it.unimib.indoornav.lab.ui.map.MapEditSession
import android.app.Application
import it.unimib.indoornav.lab.data.LabPreferences
import it.unimib.indoornav.lab.data.SessionRepository
import it.unimib.indoornav.lab.inventory.BeaconInventoryRepository
import it.unimib.indoornav.lab.service.RecordingCoordinator
import it.unimib.indoornav.lab.service.VoicePrompter
import it.unimib.indoornav.lab.survey.SurveyManager

class LabApp : Application() {
    lateinit var preferences: LabPreferences
        private set
    lateinit var sessionRepository: SessionRepository
        private set
    lateinit var beaconInventoryRepository: BeaconInventoryRepository
        private set
    val mapEditSession = MapEditSession()
    val mapFocus = MapFocusRequest()
    val mapCamera by lazy { MapCameraStore(this) }
    lateinit var recordingCoordinator: RecordingCoordinator
        private set
    lateinit var surveyManager: SurveyManager
        private set
    lateinit var voicePrompter: VoicePrompter
        private set

    override fun onCreate() {
        super.onCreate()
        preferences = LabPreferences(this)
        sessionRepository = SessionRepository(this)
        beaconInventoryRepository = BeaconInventoryRepository(this)
        recordingCoordinator = RecordingCoordinator(this)
        surveyManager = SurveyManager(this, preferences, sessionRepository)
        voicePrompter = VoicePrompter(this)

        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching { recordingCoordinator.emergencyFlushActiveWriter() }
            previousHandler?.uncaughtException(thread, throwable)
        }
    }
}
