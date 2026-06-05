package it.attendance100.mybicocca.data.repository

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.mapper.document.toAcademicTitles
import it.attendance100.mybicocca.data.mapper.document.toStudentBadge
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.document.AcademicTitle
import it.attendance100.mybicocca.domain.model.document.BadgeBlobId
import it.attendance100.mybicocca.domain.model.document.StudentBadge
import it.attendance100.mybicocca.domain.repository.DocumentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
) : DocumentRepository {

    // Badge lookup is scoped to the active career's stuId (== CareerId.value); the endpoint
    // 422s without at least one identifying query parameter. Returns the most recent card.
    override suspend fun getBadge(careerId: CareerId): StudentBadge? {
        requireCareer(careerId)
        return sessionManager.esse3().badge
            .getNewBadges(studentId = careerId.value)
            .firstOrNull()
            ?.toStudentBadge()
    }

    // The default /titoli response omits the title arrays; optionalFields=ALL is required to
    // populate SUP/TITIT/TITSTRA. (This is optionalFields, NOT the empty-object fields=ALL.)
    override suspend fun getTitles(careerId: CareerId): List<AcademicTitle> {
        requireCareer(careerId)
        val personId = requirePersonId()
        return sessionManager.esse3().personalData
            .getTitles(personId = personId, studentId = careerId.value, optionalFields = "ALL")
            .toAcademicTitles()
    }

    override suspend fun getBadgeImage(blobId: BadgeBlobId, rear: Boolean): ByteArray {
        val badge = sessionManager.esse3().badge
        val channel = if (rear) {
            badge.getBadgeBlobRearPage(blobId.value)
        } else {
            badge.getBadgeBlobFrontPage(blobId.value)
        }
        return channel.drainToByteArray()
    }

    private suspend fun ByteReadChannel.drainToByteArray(): ByteArray =
        withContext(Dispatchers.IO) { toInputStream().use { it.readBytes() } }

    private fun requireCareer(careerId: CareerId): Career {
        val account = sessionManager.activeAccount.value
            ?: error("No active account; cannot resolve career for documents.")
        return account.academic.careers.firstOrNull { it.id == careerId }
            ?: error("Career ${careerId.value} not found on active account.")
    }

    private fun requirePersonId(): Long {
        val account = sessionManager.activeAccount.value
            ?: error("No active account; cannot resolve personId for documents.")
        return account.academic.personId
    }
}
