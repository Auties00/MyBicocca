package it.attendance100.mybicocca.data.mapper.elearning

import it.attendance100.mybicocca.data.local.elearning.assignment.AssignmentEntity
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningJson
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAssignment
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningFile
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetSubmissionStatusResponse
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment
import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionForm
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionStatus
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import java.time.Instant

/**
 * JSON envelope for one file attachment reference (assignment intro/submission files,
 * forum post attachments), stored inside the entities' JSON columns. `fileUrl` already
 * carries the web-service token when it points at a webservice pluginfile endpoint.
 */
@Serializable
internal data class AttachmentRefJson(
    val fileName: String,
    val fileUrl: String? = null,
    val mimeType: String? = null,
    val sizeBytes: Long? = null,
)

/**
 * JSON envelope for the student's submission state of an assignment, persisted in the
 * assignment entity's submission-status column. Variants mirror the domain
 * SubmissionStatus; timestamps are epoch milliseconds.
 */
@Serializable
internal sealed interface SubmissionStatusJson {
    @Serializable
    data object NotSubmitted : SubmissionStatusJson

    @Serializable
    data class Draft(val savedAtMs: Long? = null) : SubmissionStatusJson

    @Serializable
    data class Submitted(
        val submittedAtMs: Long? = null,
        val files: List<AttachmentRefJson> = emptyList(),
        val onlineText: String? = null,
    ) : SubmissionStatusJson

    @Serializable
    data class Graded(
        val submittedAtMs: Long? = null,
        val grade: Double? = null,
        val maxGrade: Double? = null,
        val feedback: String? = null,
    ) : SubmissionStatusJson
}

/**
 * JSON envelope for the static submission settings, parsed from
 * mod_assign_get_assignments configs plus the assignment's own flags. Persisted
 * alongside the cached assignment; the dynamic editability (canEdit/canSubmit) and
 * current draft contents come from a fresh submission-status fetch.
 */
@Serializable
internal data class SubmissionConfigJson(
    val onlineTextEnabled: Boolean = false,
    val fileEnabled: Boolean = false,
    val maxFiles: Int = 1,
    val maxSizeBytes: Long = 0,
    val fileTypesCsv: String? = null,
    val requiresStatement: Boolean = false,
)

/**
 * Extracts the submission settings from an assignment's plugin config list, reading
 * the assignsubmission onlinetext/file entries teachers can enable per assignment.
 */
internal fun ElearningAssignment.toSubmissionConfigJson(): SubmissionConfigJson {
    fun config(plugin: String, name: String): String? = configs
        ?.firstOrNull { it.subtype == "assignsubmission" && it.plugin == plugin && it.name == name }
        ?.value
    return SubmissionConfigJson(
        onlineTextEnabled = config("onlinetext", "enabled") == "1",
        fileEnabled = config("file", "enabled") == "1",
        maxFiles = config("file", "maxfilesubmissions")?.toIntOrNull() ?: 1,
        maxSizeBytes = config("file", "maxsubmissionsizebytes")?.toLongOrNull() ?: 0,
        fileTypesCsv = config("file", "filetypeslist")?.takeIf { it.isNotBlank() },
        requiresStatement = requireSubmissionStatement == 1,
    )
}

/**
 * Maps one assignment of the mod_assign_get_assignments web service into its cache
 * row, packing the intro files (with the ws token appended to their URLs), the
 * caller-resolved submission status, and the parsed submission config into the JSON
 * columns. Epoch-second timestamps are normalized to milliseconds.
 */
internal fun ElearningAssignment.toEntity(
    accountId: AccountId,
    submissionStatus: SubmissionStatusJson,
    wsToken: String,
): AssignmentEntity {
    val introFiles = (introductionFiles.orEmpty() + introductionAttachments.orEmpty())
        .map { it.toAttachmentJson(wsToken) }
    return AssignmentEntity(
        accountId = accountId.value,
        assignmentId = id,
        courseId = courseId,
        cmId = courseModuleId,
        name = name,
        intro = introduction,
        introFilesJson = if (introFiles.isEmpty()) null else ElearningJson.encodeToString(introFiles),
        dueDateMs = dueDateTimestamp.toMillisOrNullSec(),
        allowSubmissionsFromMs = allowSubmissionsFromTimestamp.toMillisOrNullSec(),
        cutoffDateMs = cutOffDateTimestamp.toMillisOrNullSec(),
        gradingDueDateMs = gradingDueDateTimestamp.toMillisOrNullSec(),
        maxAttempts = maximumAttempts,
        allowedExtensionsCsv = null,
        allowDrafts = submissionDraftsEnabled == 1,
        submissionStatusJson = ElearningJson.encodeToString(SubmissionStatusJson.serializer(), submissionStatus),
        submissionConfigJson = ElearningJson.encodeToString(
            SubmissionConfigJson.serializer(),
            toSubmissionConfigJson(),
        ),
    )
}

/**
 * Default Italian Moodle submission statement, shown when the assignment requires
 * accepting one (the exact text is a site setting not exposed over the assignment WS).
 */
private const val DEFAULT_SUBMISSION_STATEMENT =
    "Questo elaborato è opera mia, fatta eccezione per quelle parti in cui ho indicato in modo " +
        "esplicito che si tratta del lavoro di altre persone."

/**
 * Builds the submission editor model from a fresh submission-status fetch plus the
 * cached static config. The current draft's text/files come straight from
 * lastattempt.submission.plugins so an in-progress draft pre-fills the editor, and
 * file URLs get the ws token appended for direct download.
 */
internal fun ElearningGetSubmissionStatusResponse.toSubmissionForm(
    config: SubmissionConfigJson,
    submissionDraftsEnabled: Boolean,
    wsToken: String,
): SubmissionForm {
    val attempt = lastAttempt
    val submission = attempt?.submission
    val existingText = submission?.plugins.orEmpty()
        .firstOrNull { it.type == "onlinetext" }
        ?.editorFields?.firstOrNull()?.text
        ?.takeIf { it.isNotBlank() }
    val existingFiles = submission?.plugins.orEmpty()
        .filter { it.type == "file" }
        .flatMap { plugin -> plugin.fileAreas.orEmpty().flatMap { it.files.orEmpty() } }
        .map { it.toAttachmentJson(wsToken).toDomain() }

    return SubmissionForm(
        onlineTextEnabled = config.onlineTextEnabled,
        fileEnabled = config.fileEnabled,
        maxFiles = config.maxFiles,
        maxSizeBytes = config.maxSizeBytes,
        acceptedFileTypes = config.fileTypesCsv
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            .orEmpty(),
        requiresSubmissionStatement = config.requiresStatement,
        submissionStatement = if (config.requiresStatement) DEFAULT_SUBMISSION_STATEMENT else null,
        submissionDraftsEnabled = submissionDraftsEnabled,
        canEdit = attempt?.canEdit ?: false,
        canSubmit = attempt?.canSubmit ?: false,
        existingOnlineText = existingText,
        existingFiles = existingFiles,
    )
}

/**
 * Distills a submission-status response into the persisted status envelope: graded
 * beats submitted beats draft beats nothing. Submitted files get the ws token appended
 * to their URLs; the feedback text comes from the first feedback plugin's editor field.
 */
internal fun ElearningGetSubmissionStatusResponse.toSubmissionStatusJson(wsToken: String): SubmissionStatusJson {
    val attempt = lastAttempt?.submission ?: return SubmissionStatusJson.NotSubmitted
    val submittedAtMs = (attempt.modifiedTimestamp ?: attempt.createdTimestamp).toMillisOrNullSec()
    val isGraded = lastAttempt?.graded == true
    val grade = feedback?.grade?.grade?.toDoubleOrNull()
    val feedbackText = feedback?.plugins?.firstNotNullOfOrNull { plugin ->
        plugin.editorFields?.firstOrNull()?.text
    }
    val files = attempt.plugins.orEmpty().flatMap { plugin ->
        plugin.fileAreas.orEmpty().flatMap { area -> area.files.orEmpty() }
    }.map { it.toAttachmentJson(wsToken) }
    val onlineText = attempt.plugins.orEmpty()
        .firstOrNull { it.type == "onlinetext" }
        ?.editorFields?.firstOrNull()?.text

    return when {
        isGraded -> SubmissionStatusJson.Graded(
            submittedAtMs = submittedAtMs,
            grade = grade,
            maxGrade = null,
            feedback = feedbackText,
        )
        attempt.isSubmitted -> SubmissionStatusJson.Submitted(
            submittedAtMs = submittedAtMs,
            files = files,
            onlineText = onlineText,
        )
        attempt.isDraft -> SubmissionStatusJson.Draft(savedAtMs = submittedAtMs)
        else -> SubmissionStatusJson.NotSubmitted
    }
}

/**
 * Maps a cached assignment row to the domain model, decoding the JSON intro-files and
 * submission-status columns (defensively, defaulting to no files / not submitted on
 * decode failure) and deriving the assignment's web page URL from its course-module id.
 */
internal fun AssignmentEntity.toDomain(): Assignment {
    val introFiles = introFilesJson?.let {
        runCatching {
            ElearningJson.decodeFromString(ListSerializer(AttachmentRefJson.serializer()), it)
        }.getOrNull()
    }.orEmpty().map(AttachmentRefJson::toDomain)

    val statusJson = runCatching {
        ElearningJson.decodeFromString(SubmissionStatusJson.serializer(), submissionStatusJson)
    }.getOrDefault(SubmissionStatusJson.NotSubmitted)

    val status = when (statusJson) {
        SubmissionStatusJson.NotSubmitted -> SubmissionStatus.NotSubmitted
        is SubmissionStatusJson.Draft -> SubmissionStatus.Draft(
            savedAt = statusJson.savedAtMs?.let(Instant::ofEpochMilli),
        )
        is SubmissionStatusJson.Submitted -> SubmissionStatus.Submitted(
            submittedAt = statusJson.submittedAtMs?.let(Instant::ofEpochMilli),
            files = statusJson.files.map(AttachmentRefJson::toDomain),
            onlineText = statusJson.onlineText,
        )
        is SubmissionStatusJson.Graded -> SubmissionStatus.Graded(
            submittedAt = statusJson.submittedAtMs?.let(Instant::ofEpochMilli),
            grade = statusJson.grade,
            maxGrade = statusJson.maxGrade,
            feedback = statusJson.feedback,
        )
    }

    return Assignment(
        id = AssignmentId(assignmentId),
        courseId = CourseId(courseId),
        cmId = cmId,
        name = name,
        intro = intro,
        introFiles = introFiles,
        dueDate = dueDateMs?.let(Instant::ofEpochMilli),
        allowSubmissionsFrom = allowSubmissionsFromMs?.let(Instant::ofEpochMilli),
        cutoffDate = cutoffDateMs?.let(Instant::ofEpochMilli),
        gradingDueDate = gradingDueDateMs?.let(Instant::ofEpochMilli),
        maxAttempts = maxAttempts,
        allowedExtensions = allowedExtensionsCsv?.split(",")?.filter { it.isNotBlank() }.orEmpty(),
        allowDrafts = allowDrafts,
        submissionStatus = status,
        pageUrl = cmId?.let { "$ELEARNING_BASE_URL/mod/assign/view.php?id=$it" },
    )
}

/**
 * Maps a Moodle file descriptor to the attachment envelope, falling back to the URL's
 * leaf segment when the name is missing and appending the ws token to webservice
 * pluginfile URLs.
 */
internal fun ElearningFile.toAttachmentJson(wsToken: String): AttachmentRefJson =
    AttachmentRefJson(
        fileName = fileName ?: fileUrl?.substringAfterLast('/') ?: "file",
        fileUrl = fileUrl?.let { appendWsToken(it, wsToken) },
        mimeType = mimeType,
        sizeBytes = fileSize,
    )

/**
 * Appends the ws token where it is required: webservice/pluginfile.php URLs are only
 * downloadable with the token, while browser-scope pluginfile.php URLs are left
 * untouched.
 */
private fun appendWsToken(url: String, wsToken: String): String = when {
    !url.contains("/webservice/pluginfile.php/") -> url
    url.contains("token=") -> url
    url.contains('?') -> "$url&token=$wsToken"
    else -> "$url?token=$wsToken"
}

private const val ELEARNING_BASE_URL = "https://elearning.unimib.it"

private fun AttachmentRefJson.toDomain(): Assignment.AttachmentRef =
    Assignment.AttachmentRef(
        fileName = fileName,
        fileUrl = fileUrl,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
    )

/** Converts a Moodle epoch-second timestamp to milliseconds, reading 0 as absent. */
private fun Long?.toMillisOrNullSec(): Long? = this?.takeIf { it > 0 }?.let { it * 1000L }
