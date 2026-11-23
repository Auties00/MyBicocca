package it.unimib.indoornav.lab.data

import android.content.Context
import it.unimib.indoornav.lab.format.ExperimentTemplate
import it.unimib.indoornav.lab.format.ProjectConstants
import it.unimib.indoornav.lab.ui.model.ModelRenderer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class SetupConfig(
    val operator: String = "",
    val marker: String = "",
    val phoneLabel: String = "A1",
    val building: String = ProjectConstants.DEFAULT_BUILDING,
    val buildingCode: Int = ProjectConstants.DEFAULT_BUILDING_CODE,
    val floor: Int = ProjectConstants.DEFAULT_FLOOR,
    val templateCode: String = ExperimentTemplate.E5.code,
    val carryMode: String = ExperimentTemplate.E5.defaultCarryMode,
    val recordingMode: String = ExperimentTemplate.E5.defaultRecordingMode,
    val templateFields: Map<String, String> = mapOf(
        "route" to "R1",
        "dir" to "out",
        "pace" to "normal",
        "mode" to ExperimentTemplate.E5.defaultCarryMode,
        "layout" to "1",
    ),
    val notes: String = "",
    val allowScreenOff: Boolean = false,
    val bleInventoryMode: Boolean = false,
    val bleStoreEverything: Boolean = false,
    val universitySsids: List<String> = ProjectConstants.DEFAULT_UNIVERSITY_SSIDS,
    val modelRenderer: ModelRenderer = ModelRenderer.GL,
)

class LabPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("lab_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    private val _config = MutableStateFlow(load())
    val config: StateFlow<SetupConfig> = _config.asStateFlow()

    private fun load(): SetupConfig {
        val fieldsRaw = prefs.getString(KEY_TEMPLATE_FIELDS, null)
        val fields = if (fieldsRaw != null) {
            runCatching { json.decodeFromString<Map<String, String>>(fieldsRaw) }.getOrDefault(emptyMap())
        } else {
            mapOf(
                "route" to "R1",
                "dir" to "out",
                "pace" to "normal",
                "mode" to ExperimentTemplate.E5.defaultCarryMode,
                "layout" to "1",
            )
        }

        val ssidsRaw = prefs.getString(KEY_UNIVERSITY_SSIDS, null)
        val ssids = if (ssidsRaw != null) {
            runCatching { json.decodeFromString<List<String>>(ssidsRaw) }
                .getOrDefault(ProjectConstants.DEFAULT_UNIVERSITY_SSIDS)
                .ifEmpty { ProjectConstants.DEFAULT_UNIVERSITY_SSIDS }
        } else {
            ProjectConstants.DEFAULT_UNIVERSITY_SSIDS
        }

        val rendererRaw = prefs.getString(KEY_MODEL_RENDERER, ModelRenderer.GL.name)
        val modelRenderer = ModelRenderer.entries.firstOrNull { it.name == rendererRaw } ?: ModelRenderer.GL

        val raw = SetupConfig(
            operator = prefs.getString(KEY_OPERATOR, "") ?: "",
            marker = prefs.getString(KEY_MARKER, "") ?: "",
            phoneLabel = prefs.getString(KEY_PHONE_LABEL, "A1") ?: "A1",
            building = prefs.getString(KEY_BUILDING, ProjectConstants.DEFAULT_BUILDING)
                ?: ProjectConstants.DEFAULT_BUILDING,
            buildingCode = prefs.getInt(KEY_BUILDING_CODE, ProjectConstants.DEFAULT_BUILDING_CODE),
            floor = prefs.getInt(KEY_FLOOR, ProjectConstants.DEFAULT_FLOOR),
            templateCode = prefs.getString(KEY_TEMPLATE_CODE, ExperimentTemplate.E5.code)
                ?: ExperimentTemplate.E5.code,
            carryMode = prefs.getString(KEY_CARRY_MODE, ExperimentTemplate.E5.defaultCarryMode)
                ?: ExperimentTemplate.E5.defaultCarryMode,
            recordingMode = prefs.getString(KEY_RECORDING_MODE, ExperimentTemplate.E5.defaultRecordingMode)
                ?: ExperimentTemplate.E5.defaultRecordingMode,
            templateFields = fields,
            notes = prefs.getString(KEY_NOTES, "") ?: "",
            allowScreenOff = prefs.getBoolean(KEY_ALLOW_SCREEN_OFF, false),
            bleInventoryMode = prefs.getBoolean(KEY_BLE_INVENTORY, false),
            bleStoreEverything = prefs.getBoolean(KEY_BLE_STORE_ALL, false),
            universitySsids = ssids,
            modelRenderer = modelRenderer,
        )
        return normalizeConfig(raw)
    }

    private fun normalizeConfig(raw: SetupConfig): SetupConfig {
        val tmpl = ExperimentTemplate.fromCode(raw.templateCode) ?: ExperimentTemplate.E5
        val recordingMode = if (raw.recordingMode in tmpl.allowedRecordingModes) {
            raw.recordingMode
        } else {
            tmpl.defaultRecordingMode
        }
        val nextFields = raw.templateFields.toMutableMap()
        val carryMode = if (tmpl.hasModeField()) {
            val modeField = tmpl.fields.first { it.key == "mode" }
            val resolved = nextFields["mode"]?.takeIf { it.isNotBlank() }
                ?: raw.carryMode.takeIf { it in modeField.suggestions }
                ?: tmpl.defaultCarryMode
            nextFields["mode"] = resolved
            resolved
        } else {
            raw.carryMode
        }
        return raw.copy(
            templateCode = tmpl.code,
            recordingMode = recordingMode,
            carryMode = carryMode,
            templateFields = nextFields,
        )
    }

    fun update(transform: (SetupConfig) -> SetupConfig) {
        val next = transform(_config.value)
        _config.value = next
        prefs.edit()
            .putString(KEY_OPERATOR, next.operator)
            .putString(KEY_MARKER, next.marker)
            .putString(KEY_PHONE_LABEL, next.phoneLabel)
            .putString(KEY_BUILDING, next.building)
            .putInt(KEY_BUILDING_CODE, next.buildingCode)
            .putInt(KEY_FLOOR, next.floor)
            .putString(KEY_TEMPLATE_CODE, next.templateCode)
            .putString(KEY_CARRY_MODE, next.carryMode)
            .putString(KEY_RECORDING_MODE, next.recordingMode)
            .putString(KEY_TEMPLATE_FIELDS, json.encodeToString(next.templateFields))
            .putString(KEY_NOTES, next.notes)
            .putBoolean(KEY_ALLOW_SCREEN_OFF, next.allowScreenOff)
            .putBoolean(KEY_BLE_INVENTORY, next.bleInventoryMode)
            .putBoolean(KEY_BLE_STORE_ALL, next.bleStoreEverything)
            .putString(KEY_UNIVERSITY_SSIDS, json.encodeToString(next.universitySsids))
            .putString(KEY_MODEL_RENDERER, next.modelRenderer.name)
            .apply()
    }

    private companion object {
        const val KEY_OPERATOR = "operator"
        const val KEY_MARKER = "marker"
        const val KEY_PHONE_LABEL = "phone_label"
        const val KEY_BUILDING = "building"
        const val KEY_BUILDING_CODE = "building_code"
        const val KEY_FLOOR = "floor"
        const val KEY_TEMPLATE_CODE = "template_code"
        const val KEY_CARRY_MODE = "carry_mode"
        const val KEY_RECORDING_MODE = "recording_mode"
        const val KEY_TEMPLATE_FIELDS = "template_fields"
        const val KEY_NOTES = "notes"
        const val KEY_ALLOW_SCREEN_OFF = "allow_screen_off"
        const val KEY_BLE_INVENTORY = "ble_inventory"
        const val KEY_BLE_STORE_ALL = "ble_store_all"
        const val KEY_UNIVERSITY_SSIDS = "university_ssids"
        const val KEY_MODEL_RENDERER = "model_renderer"
    }
}
