package it.attendance100.mybicocca.domain.usecase.document

import it.attendance100.mybicocca.domain.model.document.BadgeBlobId
import it.attendance100.mybicocca.domain.repository.DocumentRepository
import javax.inject.Inject

class GetBadgeImageUseCase @Inject constructor(
    private val repository: DocumentRepository,
) {
    suspend operator fun invoke(blobId: BadgeBlobId, rear: Boolean): ByteArray =
        repository.getBadgeImage(blobId, rear)
}
