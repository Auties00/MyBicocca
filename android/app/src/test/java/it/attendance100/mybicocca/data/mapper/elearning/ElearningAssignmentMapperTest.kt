package it.attendance100.mybicocca.data.mapper.elearning

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.local.elearning.assignment.AssignmentEntity
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAssignment
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAssignmentConfig
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAssignmentFeedback
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAssignmentGrade
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAssignmentSubmission
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningEditorField
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningFile
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetSubmissionStatusResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningLastAttempt
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningSubmissionFileArea
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningSubmissionPlugin
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionStatus
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import org.junit.Test
import java.time.Instant

/**
 * Covers the mod_assign mappers: submission-config extraction from plugin configs, the
 * ws-token append rule on pluginfile URLs, the status precedence (graded > submitted >
 * draft > nothing), the submission-form assembly (existing draft, accepted file types,
 * statement), and the cached-assignment round-trip with defensive status decoding plus the
 * derived page URL.
 */
class ElearningAssignmentMapperTest {

    private val account = AccountId("acc-1")
    private val token = "WSTOKEN"

    private fun config(plugin: String, name: String, value: String) =
        ElearningAssignmentConfig(plugin = plugin, subtype = "assignsubmission", name = name, value = value)

    private fun assignment(
        configs: List<ElearningAssignmentConfig>? = null,
        requireStatement: Int? = 0,
        submissionDrafts: Int? = 1,
        cmId: Int? = 77,
        introFiles: List<ElearningFile>? = null,
        introAttachments: List<ElearningFile>? = null,
    ) = ElearningAssignment(
        id = 10,
        courseModuleId = cmId,
        courseId = 5,
        name = "Compito 1",
        introduction = "<p>intro</p>",
        introductionFiles = introFiles,
        introductionAttachments = introAttachments,
        dueDateTimestamp = 1_000L,
        allowSubmissionsFromTimestamp = 500L,
        cutOffDateTimestamp = 2_000L,
        gradingDueDateTimestamp = 3_000L,
        maximumAttempts = -1,
        submissionDraftsEnabled = submissionDrafts,
        requireSubmissionStatement = requireStatement,
        configs = configs,
    )

    @Test
    fun `submission config reads the enabled online-text and file plugins`() {
        val cfg = assignment(
            configs = listOf(
                config("onlinetext", "enabled", "1"),
                config("file", "enabled", "1"),
                config("file", "maxfilesubmissions", "3"),
                config("file", "maxsubmissionsizebytes", "1048576"),
                config("file", "filetypeslist", ".pdf,.docx"),
            ),
            requireStatement = 1,
        ).toSubmissionConfigJson()
        assertThat(cfg.onlineTextEnabled).isTrue()
        assertThat(cfg.fileEnabled).isTrue()
        assertThat(cfg.maxFiles).isEqualTo(3)
        assertThat(cfg.maxSizeBytes).isEqualTo(1_048_576L)
        assertThat(cfg.fileTypesCsv).isEqualTo(".pdf,.docx")
        assertThat(cfg.requiresStatement).isTrue()
    }

    @Test
    fun `submission config falls back to defaults when configs are absent`() {
        val cfg = assignment(configs = null, requireStatement = 0).toSubmissionConfigJson()
        assertThat(cfg.onlineTextEnabled).isFalse()
        assertThat(cfg.fileEnabled).isFalse()
        assertThat(cfg.maxFiles).isEqualTo(1)
        assertThat(cfg.maxSizeBytes).isEqualTo(0L)
        assertThat(cfg.fileTypesCsv).isNull()
        assertThat(cfg.requiresStatement).isFalse()
    }

    @Test
    fun `submission config blanks an empty file-types list`() {
        val cfg = assignment(configs = listOf(config("file", "filetypeslist", "  "))).toSubmissionConfigJson()
        assertThat(cfg.fileTypesCsv).isNull()
    }

    @Test
    fun `attachment json appends the ws token to webservice pluginfile urls`() {
        val file = ElearningFile(fileName = "a.pdf", fileUrl = "https://m/webservice/pluginfile.php/1/x")
        val ref = file.toAttachmentJson(token)
        assertThat(ref.fileUrl).isEqualTo("https://m/webservice/pluginfile.php/1/x?token=WSTOKEN")
    }

    @Test
    fun `attachment json appends the ws token with an ampersand when a query exists`() {
        val file = ElearningFile(fileName = "a.pdf", fileUrl = "https://m/webservice/pluginfile.php/1/x?forcedownload=1")
        assertThat(file.toAttachmentJson(token).fileUrl)
            .isEqualTo("https://m/webservice/pluginfile.php/1/x?forcedownload=1&token=WSTOKEN")
    }

    @Test
    fun `attachment json leaves a browser-scope pluginfile url untouched`() {
        val file = ElearningFile(fileName = "a.pdf", fileUrl = "https://m/pluginfile.php/1/x")
        assertThat(file.toAttachmentJson(token).fileUrl).isEqualTo("https://m/pluginfile.php/1/x")
    }

    @Test
    fun `attachment json does not double-append an existing token`() {
        val file = ElearningFile(fileName = "a.pdf", fileUrl = "https://m/webservice/pluginfile.php/1/x?token=OLD")
        assertThat(file.toAttachmentJson(token).fileUrl)
            .isEqualTo("https://m/webservice/pluginfile.php/1/x?token=OLD")
    }

    @Test
    fun `attachment json derives the name from the url leaf when missing`() {
        val file = ElearningFile(fileName = null, fileUrl = "https://m/path/report.pdf")
        assertThat(file.toAttachmentJson(token).fileName).isEqualTo("report.pdf")
    }

    @Test
    fun `attachment json names an unidentifiable file plainly`() {
        val file = ElearningFile(fileName = null, fileUrl = null)
        assertThat(file.toAttachmentJson(token).fileName).isEqualTo("file")
    }

    private fun response(
        submission: ElearningAssignmentSubmission? = null,
        graded: Boolean? = false,
        feedback: ElearningAssignmentFeedback? = null,
    ) = ElearningGetSubmissionStatusResponse(
        lastAttempt = ElearningLastAttempt(
            submission = submission,
            graded = graded,
            canEdit = true,
            canSubmit = false,
        ),
        feedback = feedback,
    )

    @Test
    fun `status json is not-submitted with no attempt`() {
        val json = ElearningGetSubmissionStatusResponse().toSubmissionStatusJson(token)
        assertThat(json).isInstanceOf(SubmissionStatusJson.NotSubmitted::class.java)
    }

    @Test
    fun `status json is submitted for a submitted attempt`() {
        val submission = ElearningAssignmentSubmission(
            id = 1,
            status = "submitted",
            modifiedTimestamp = 5_000L,
            plugins = listOf(
                ElearningSubmissionPlugin(
                    type = "onlinetext",
                    editorFields = listOf(ElearningEditorField(text = "la mia risposta")),
                ),
            ),
        )
        val json = response(submission = submission, graded = false).toSubmissionStatusJson(token)
        assertThat(json).isInstanceOf(SubmissionStatusJson.Submitted::class.java)
        json as SubmissionStatusJson.Submitted
        assertThat(json.submittedAtMs).isEqualTo(5_000_000L)
        assertThat(json.onlineText).isEqualTo("la mia risposta")
    }

    @Test
    fun `status json is draft for a draft attempt`() {
        val submission = ElearningAssignmentSubmission(id = 1, status = "draft", createdTimestamp = 4_000L)
        val json = response(submission = submission, graded = false).toSubmissionStatusJson(token)
        assertThat(json).isInstanceOf(SubmissionStatusJson.Draft::class.java)
        assertThat((json as SubmissionStatusJson.Draft).savedAtMs).isEqualTo(4_000_000L)
    }

    @Test
    fun `status json graded beats submitted`() {
        val submission = ElearningAssignmentSubmission(id = 1, status = "submitted", modifiedTimestamp = 5_000L)
        val feedback = ElearningAssignmentFeedback(
            grade = ElearningAssignmentGrade(id = 1, grade = "27.0"),
            plugins = listOf(
                ElearningSubmissionPlugin(
                    type = "comments",
                    editorFields = listOf(ElearningEditorField(text = "ottimo")),
                ),
            ),
        )
        val json = response(submission = submission, graded = true, feedback = feedback).toSubmissionStatusJson(token)
        assertThat(json).isInstanceOf(SubmissionStatusJson.Graded::class.java)
        json as SubmissionStatusJson.Graded
        assertThat(json.grade).isEqualTo(27.0)
        assertThat(json.feedback).isEqualTo("ottimo")
    }

    @Test
    fun `status json is not-submitted for a new attempt`() {
        val submission = ElearningAssignmentSubmission(id = 1, status = "new")
        val json = response(submission = submission, graded = false).toSubmissionStatusJson(token)
        assertThat(json).isInstanceOf(SubmissionStatusJson.NotSubmitted::class.java)
    }

    @Test
    fun `submission form pre-fills existing online text and files`() {
        val response = ElearningGetSubmissionStatusResponse(
            lastAttempt = ElearningLastAttempt(
                submission = ElearningAssignmentSubmission(
                    id = 1,
                    status = "draft",
                    plugins = listOf(
                        ElearningSubmissionPlugin(
                            type = "onlinetext",
                            editorFields = listOf(ElearningEditorField(text = "bozza")),
                        ),
                        ElearningSubmissionPlugin(
                            type = "file",
                            fileAreas = listOf(
                                ElearningSubmissionFileArea(
                                    area = "submission_files",
                                    files = listOf(ElearningFile(fileName = "draft.pdf", fileUrl = "u")),
                                ),
                            ),
                        ),
                    ),
                ),
                canEdit = true,
                canSubmit = true,
            ),
        )
        val config = SubmissionConfigJson(
            onlineTextEnabled = true,
            fileEnabled = true,
            maxFiles = 2,
            maxSizeBytes = 1024,
            fileTypesCsv = ".pdf, .docx ,",
            requiresStatement = true,
        )
        val form = response.toSubmissionForm(config, submissionDraftsEnabled = true, wsToken = token)
        assertThat(form.existingOnlineText).isEqualTo("bozza")
        assertThat(form.existingFiles.map { it.fileName }).containsExactly("draft.pdf")
        assertThat(form.canEdit).isTrue()
        assertThat(form.canSubmit).isTrue()
        assertThat(form.acceptedFileTypes).containsExactly(".pdf", ".docx").inOrder()
        assertThat(form.submissionStatement).isNotNull()
    }

    @Test
    fun `submission form blanks an empty online text and omits the statement when not required`() {
        val response = ElearningGetSubmissionStatusResponse(
            lastAttempt = ElearningLastAttempt(
                submission = ElearningAssignmentSubmission(
                    id = 1,
                    status = "draft",
                    plugins = listOf(
                        ElearningSubmissionPlugin(
                            type = "onlinetext",
                            editorFields = listOf(ElearningEditorField(text = "   ")),
                        ),
                    ),
                ),
            ),
        )
        val config = SubmissionConfigJson(onlineTextEnabled = true, requiresStatement = false)
        val form = response.toSubmissionForm(config, submissionDraftsEnabled = false, wsToken = token)
        assertThat(form.existingOnlineText).isNull()
        assertThat(form.submissionStatement).isNull()
        assertThat(form.acceptedFileTypes).isEmpty()
        assertThat(form.canEdit).isFalse()
        assertThat(form.canSubmit).isFalse()
    }

    @Test
    fun `assignment toEntity merges intro files and attachments and normalizes dates`() {
        val entity = assignment(
            introFiles = listOf(ElearningFile(fileName = "syllabus.pdf", fileUrl = "u1")),
            introAttachments = listOf(ElearningFile(fileName = "rubric.pdf", fileUrl = "u2")),
            submissionDrafts = 1,
        ).toEntity(account, SubmissionStatusJson.NotSubmitted, token)
        assertThat(entity.assignmentId).isEqualTo(10)
        assertThat(entity.courseId).isEqualTo(5)
        assertThat(entity.cmId).isEqualTo(77)
        assertThat(entity.dueDateMs).isEqualTo(1_000_000L)
        assertThat(entity.allowSubmissionsFromMs).isEqualTo(500_000L)
        assertThat(entity.allowDrafts).isTrue()
        assertThat(entity.introFilesJson).contains("syllabus.pdf")
        assertThat(entity.introFilesJson).contains("rubric.pdf")
    }

    @Test
    fun `assignment toEntity leaves the intro files column null when there are none`() {
        val entity = assignment().toEntity(account, SubmissionStatusJson.NotSubmitted, token)
        assertThat(entity.introFilesJson).isNull()
    }

    @Test
    fun `assignment entity toDomain decodes status and derives the page url`() {
        val source = assignment(cmId = 77).toEntity(
            account,
            SubmissionStatusJson.Submitted(submittedAtMs = 5_000_000L, files = emptyList(), onlineText = "x"),
            token,
        )
        val domain = source.toDomain()
        assertThat(domain.id).isEqualTo(AssignmentId(10))
        assertThat(domain.courseId).isEqualTo(CourseId(5))
        assertThat(domain.submissionStatus).isInstanceOf(SubmissionStatus.Submitted::class.java)
        assertThat((domain.submissionStatus as SubmissionStatus.Submitted).submittedAt)
            .isEqualTo(Instant.ofEpochMilli(5_000_000L))
        assertThat(domain.pageUrl).isEqualTo("https://elearning.unimib.it/mod/assign/view.php?id=77")
    }

    @Test
    fun `assignment entity toDomain leaves the page url null without a course-module id`() {
        val source = assignment(cmId = null).toEntity(account, SubmissionStatusJson.NotSubmitted, token)
        assertThat(source.toDomain().pageUrl).isNull()
    }

    @Test
    fun `assignment entity toDomain falls back to not-submitted on a malformed status blob`() {
        val source = assignment().toEntity(account, SubmissionStatusJson.NotSubmitted, token)
        val corrupt = source.copy(submissionStatusJson = "{bad json")
        assertThat(corrupt.toDomain().submissionStatus).isEqualTo(SubmissionStatus.NotSubmitted)
    }

    @Test
    fun `assignment entity toDomain drops the intro files on a malformed blob`() {
        val source = assignment(
            introFiles = listOf(ElearningFile(fileName = "x.pdf", fileUrl = "u")),
        ).toEntity(account, SubmissionStatusJson.NotSubmitted, token)
        val corrupt = source.copy(introFilesJson = "{bad json")
        assertThat(corrupt.toDomain().introFiles).isEmpty()
    }

    @Test
    fun `assignment entity toDomain splits the allowed extensions csv dropping blanks`() {
        val source = assignment().toEntity(account, SubmissionStatusJson.NotSubmitted, token)
        val entity: AssignmentEntity = source.copy(allowedExtensionsCsv = ".pdf,, .docx")
        assertThat(entity.toDomain().allowedExtensions).containsExactly(".pdf", " .docx").inOrder()
    }
}
