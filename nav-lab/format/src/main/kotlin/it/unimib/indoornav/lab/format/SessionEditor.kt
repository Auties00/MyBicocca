package it.unimib.indoornav.lab.format

/** What the user may correct about a recording after it has finished. */
data class RecordingEdits(
    val operator: String,
    val marker: String,
    val phoneLabel: String,
    val notes: String,
    val templateFields: Map<String, String>,
)

object SessionEditor {
    fun editsOf(manifest: SessionManifest): RecordingEdits = RecordingEdits(
        operator = manifest.setup.operator,
        marker = manifest.setup.marker,
        phoneLabel = manifest.phone.label,
        notes = manifest.setup.notes,
        templateFields = manifest.setup.template_fields,
    )

    /** Applies [edits], rebuilding the test ID and carry mode from the template fields as a new recording would. */
    fun apply(manifest: SessionManifest, template: ExperimentTemplate?, edits: RecordingEdits): SessionManifest {
        val fields = edits.templateFields.mapValues { it.value.trim() }
        val testId = template?.let { TestIdCodec.build(it, fields) } ?: manifest.setup.test_id
        val carryMode = if (template?.hasModeField() == true) {
            fields["mode"]?.takeIf { it.isNotEmpty() } ?: manifest.setup.carry_mode
        } else {
            manifest.setup.carry_mode
        }
        return manifest.copy(
            phone = manifest.phone.copy(label = edits.phoneLabel.trim().ifEmpty { manifest.phone.label }),
            setup = manifest.setup.copy(
                operator = edits.operator.trim(),
                marker = edits.marker.trim(),
                notes = edits.notes.trim(),
                template_fields = fields,
                test_id = testId,
                carry_mode = carryMode,
            ),
        )
    }

    /** The folder name a recording with this manifest gets, so a renamed test ID renames the folder too. */
    fun folderNameFor(manifest: SessionManifest): String =
        SessionNaming.buildFolderName(manifest.clock.wall_ms, manifest.phone.label, manifest.setup.test_id)
}
