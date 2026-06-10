package it.attendance100.mybicocca.domain.usecase.document

import it.attendance100.mybicocca.domain.model.document.BadgeBlobId
import it.attendance100.mybicocca.domain.repository.DocumentRepository
import javax.inject.Inject

/**
 * Downloads the front or rear artwork of the student card from Esse3, returning the raw image
 * bytes. Only meaningful when the badge exposes a blob id and the matching image flag.
 */
class GetBadgeImageUseCase @Inject constructor(
    private val repository: DocumentRepository,
) {
    suspend operator fun invoke(blobId: BadgeBlobId, rear: Boolean): ByteArray =
        repository.getBadgeImage(blobId, rear)
}
