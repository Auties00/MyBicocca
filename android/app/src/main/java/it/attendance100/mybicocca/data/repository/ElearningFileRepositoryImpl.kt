package it.attendance100.mybicocca.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.utils.io.jvm.javaio.toInputStream
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.domain.repository.ElearningFileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * File repository backed by the tokenized Moodle pluginfile endpoint and a cache
 * directory under the app's private storage. Each URL gets its own cache folder named
 * by a hash of the URL, holding the file under its sanitized original name; downloads
 * stream through a temp file and are promoted atomically, so the existence check that
 * implements the cache hit can never see a truncated file left by a death
 * mid-download. One mutex per cache key makes concurrent requests for the same file
 * share a single download while different files still download in parallel.
 */
@Singleton
class ElearningFileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager,
) : ElearningFileRepository {

    private val downloadLocks = ConcurrentHashMap<String, Mutex>()

    override suspend fun downloadFile(fileUrl: String, fileName: String): String {
        val key = cacheKey(fileUrl)
        val file = File(context.cacheDir, "$FILES_DIR/$key/${sanitize(fileName)}")
        downloadLocks.computeIfAbsent(key) { Mutex() }.withLock {
            if (file.exists() && file.length() > 0) return file.absolutePath

            file.parentFile?.mkdirs()
            val (api, token) = sessionManager.elearning()
            val channel = api.files.downloadFile(token, fileUrl)
            val temp = File(file.parentFile, "${file.name}.part")
            withContext(Dispatchers.IO) {
                channel.toInputStream().use { input ->
                    temp.outputStream().use { output -> input.copyTo(output) }
                }
                if (!temp.renameTo(file)) {
                    temp.delete()
                    error("Impossibile salvare il file scaricato.")
                }
            }
        }
        return file.absolutePath
    }

    override suspend fun authenticatedFileUrl(fileUrl: String): String {
        val (api, token) = sessionManager.elearning()
        return api.files.authenticatedFileUrl(token, fileUrl)
    }

    /**
     * Keyed on the URL (not the name) so same-named files from different modules never
     * collide; the original file name is kept as the leaf so renderers and external
     * apps see the right extension.
     */
    private fun cacheKey(fileUrl: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(fileUrl.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }.take(16)
    }

    private fun sanitize(fileName: String): String =
        fileName.replace(Regex("""[\\/:*?"<>|]"""), "_").ifBlank { "file" }

    private companion object {
        const val FILES_DIR = "elearning_files"
    }
}
