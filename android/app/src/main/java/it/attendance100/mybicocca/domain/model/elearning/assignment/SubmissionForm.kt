package it.attendance100.mybicocca.domain.model.elearning.assignment

/**
 * Everything the in-app submission (consegna) editor needs: which submission types the
 * assignment accepts, the file constraints, whether a submission statement must be accepted to
 * finalize, and the current draft contents so the editor pre-fills an in-progress hand-in.
 * Assembled from Moodle's mod_assign web services and fetched fresh each time the editor opens
 * — never cached — because editability is time- and lock-sensitive.
 *
 * @property onlineTextEnabled Whether the online-text submission type is enabled.
 * @property fileEnabled Whether the file submission type is enabled.
 * @property maxFiles Maximum number of files accepted.
 * @property maxSizeBytes Maximum size per file in bytes; 0 when no explicit per-assignment
 * limit is configured.
 * @property acceptedFileTypes Raw Moodle filetypeslist tokens — a mix of group names (archive,
 * image, document) and explicit extensions (.pdf). Empty means any type is accepted.
 * @property requiresSubmissionStatement Whether finalizing requires accepting [submissionStatement].
 * @property submissionStatement Statement text to present for acceptance, when one is required.
 * @property submissionDraftsEnabled Whether saves are kept as drafts until explicitly finalized.
 * @property canEdit Whether the student may create or modify a (draft) submission right now.
 * @property canSubmit Whether there is something to finalize for grading right now.
 * @property existingOnlineText Online text already saved in the current submission, when any.
 * @property existingFiles Files already part of the current submission.
 */
data class SubmissionForm(
    val onlineTextEnabled: Boolean,
    val fileEnabled: Boolean,
    val maxFiles: Int,
    val maxSizeBytes: Long,
    val acceptedFileTypes: List<String>,
    val requiresSubmissionStatement: Boolean,
    val submissionStatement: String?,
    val submissionDraftsEnabled: Boolean,
    val canEdit: Boolean,
    val canSubmit: Boolean,
    val existingOnlineText: String?,
    val existingFiles: List<Assignment.AttachmentRef>,
)
