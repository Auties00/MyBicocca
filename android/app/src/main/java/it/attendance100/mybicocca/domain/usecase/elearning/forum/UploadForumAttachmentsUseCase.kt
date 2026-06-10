package it.attendance100.mybicocca.domain.usecase.elearning.forum

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumAttachmentUpload
import it.attendance100.mybicocca.domain.repository.ElearningForumRepository
import javax.inject.Inject

/**
 * Uploads the files picked in the forum sheet's composer to a Moodle draft area and returns the
 * draft-area id to pass as the post's `attachmentsid`. Pass a base draft id to add to an
 * existing draft — e.g. a post's prepared attachment area on edit — otherwise a fresh draft area
 * is created. Throws when the upload fails.
 */
class UploadForumAttachmentsUseCase @Inject constructor(
    private val repository: ElearningForumRepository,
) {
    suspend operator fun invoke(
        accountId: AccountId,
        files: List<ForumAttachmentUpload>,
        baseDraftItemId: Int? = null,
    ): Int = repository.uploadAttachments(accountId, files, baseDraftItemId)
}
