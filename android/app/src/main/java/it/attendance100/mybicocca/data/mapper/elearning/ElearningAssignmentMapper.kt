package it.attendance100.mybicocca.data.mapper.elearning

import it.attendance100.mybicocca.data.local.elearning.assignment.AssignmentEntity
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningJson
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAssignment
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningFile
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetSubmissionStatusResponse
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment
import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionStatus
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import java.time.Instant

@Serializable
internal data class AttachmentRefJson(
    val fileName: String,
    val fileUrl: String? = null,
    val mimeType: String? = null,
    val sizeBytes: Long? = null,
)

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

internal fun ElearningAssignment.toEntity(
    accountId: AccountId,
    submissionStatus: SubmissionStatusJson,
): AssignmentEntity {
    val introFiles = (introductionFiles.orEmpty() + introductionAttachments.orEmpty())
        .map(ElearningFile::toAttachmentJson)
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
    )
}

internal fun ElearningGetSubmissionStatusResponse.toSubmissionStatusJson(): SubmissionStatusJson {
    val attempt = lastAttempt?.submission ?: return SubmissionStatusJson.NotSubmitted
    val submittedAtMs = (attempt.modifiedTimestamp ?: attempt.createdTimestamp).toMillisOrNullSec()
    val isGraded = lastAttempt?.graded == true
    val grade = feedback?.grade?.grade?.toDoubleOrNull()
    val feedbackText = feedback?.plugins?.firstNotNullOfOrNull { plugin ->
        plugin.editorFields?.firstOrNull()?.text
    }
    val files = attempt.plugins.orEmpty().flatMap { plugin ->
        plugin.fileAreas.orEmpty().flatMap { area -> area.files.orEmpty() }
    }.map(ElearningFile::toAttachmentJson)
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
    )
}

internal fun ElearningFile.toAttachmentJson(): AttachmentRefJson =
    AttachmentRefJson(
        fileName = fileName ?: fileUrl?.substringAfterLast('/') ?: "file",
        fileUrl = fileUrl,
        mimeType = mimeType,
        sizeBytes = fileSize,
    )

private fun AttachmentRefJson.toDomain(): Assignment.AttachmentRef =
    Assignment.AttachmentRef(
        fileName = fileName,
        fileUrl = fileUrl,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
    )

private fun Long?.toMillisOrNullSec(): Long? = this?.takeIf { it > 0 }?.let { it * 1000L }
