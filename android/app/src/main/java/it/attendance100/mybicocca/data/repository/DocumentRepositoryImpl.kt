package it.attendance100.mybicocca.data.repository

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.document.BadgeImageEntity
import it.attendance100.mybicocca.data.local.document.DocumentCacheDao
import it.attendance100.mybicocca.data.mapper.document.toAcademicTitles
import it.attendance100.mybicocca.data.mapper.document.toAttributeEntities
import it.attendance100.mybicocca.data.mapper.document.toDomain
import it.attendance100.mybicocca.data.mapper.document.toEntity
import it.attendance100.mybicocca.data.mapper.document.toStudentBadge
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.document.AcademicTitle
import it.attendance100.mybicocca.domain.model.document.BadgeBlobId
import it.attendance100.mybicocca.domain.model.document.StudentBadge
import it.attendance100.mybicocca.domain.repository.DocumentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Live implementation over the Esse3 badge and personal-data endpoints. Identifiers are
 * resolved from the active account (personId) and the requested career (stuId equals
 * CareerId.value), which must belong to it. Every read — badge record, badge images, titles —
 * is live-first and written through to the offline document mirror ([DocumentCacheDao]); the
 * mirror is read back only when a live call fails with an [IOException] (no network), so the
 * student card stays viewable offline. A null live badge clears its cached row (the absence is
 * a valid result, not an error); an offline read with no cached data rethrows.
 */
@Singleton
class DocumentRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val documentCacheDao: DocumentCacheDao,
) : DocumentRepository {

    /**
     * Badge lookup is scoped to the active career's stuId; the endpoint 422s without at least
     * one identifying query parameter. Returns the most recent card, or null when the career has
     * none — a null result clears the cache so the absence is mirrored, and only an offline
     * failure falls back to the cached card.
     */
    override suspend fun getBadge(careerId: CareerId): StudentBadge? {
        requireCareer(careerId)
        return try {
            val badge = sessionManager.esse3().badge
                .getNewBadges(studentId = careerId.value)
                .firstOrNull()
                ?.toStudentBadge()
            documentCacheDao.replaceBadge(careerId.value, badge?.toEntity(careerId))
            badge
        } catch (e: IOException) {
            documentCacheDao.getBadge(careerId.value)?.toDomain() ?: throw e
        }
    }

    /**
     * The default /titoli response omits the title arrays; optionalFields=ALL is required to
     * populate SUP/TITIT/TITSTRA. (This is optionalFields, NOT the empty-object fields=ALL.)
     */
    override suspend fun getTitles(careerId: CareerId): List<AcademicTitle> {
        requireCareer(careerId)
        val personId = requirePersonId()
        return try {
            val titles = sessionManager.esse3().personalData
                .getTitles(personId = personId, studentId = careerId.value, optionalFields = "ALL")
                .toAcademicTitles()
            documentCacheDao.replaceTitles(
                careerId.value,
                titles.mapIndexed { index, title -> title.toEntity(careerId, index) },
                titles.flatMap { it.toAttributeEntities(careerId) },
            )
            titles
        } catch (e: IOException) {
            readCachedTitles(careerId).ifEmpty { throw e }
        }
    }

    override suspend fun getBadgeImage(blobId: BadgeBlobId, rear: Boolean): ByteArray {
        val side = if (rear) "rear" else "front"
        return try {
            val badge = sessionManager.esse3().badge
            val channel = if (rear) {
                badge.getBadgeBlobRearPage(blobId.value)
            } else {
                badge.getBadgeBlobFrontPage(blobId.value)
            }
            val bytes = channel.drainToByteArray()
            documentCacheDao.upsertImage(BadgeImageEntity(blobId.value, side, bytes))
            bytes
        } catch (e: IOException) {
            documentCacheDao.getImage(blobId.value, side)?.bytes ?: throw e
        }
    }

    // Rebuilds the cached titles by re-attaching each title's ordered attribute rows.
    private suspend fun readCachedTitles(careerId: CareerId): List<AcademicTitle> {
        val attributesByTitle = documentCacheDao.getTitleAttributes(careerId.value)
            .groupBy { it.titleId }
        return documentCacheDao.getTitles(careerId.value).map { title ->
            val attributes = attributesByTitle[title.titleId]
                .orEmpty()
                .mapNotNull { it.toDomain() }
            title.toDomain(attributes)
        }
    }

    /** Fully reads a streamed image response into memory off the main thread. */
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
