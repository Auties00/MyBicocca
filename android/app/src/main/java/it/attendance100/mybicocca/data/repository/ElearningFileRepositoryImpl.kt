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

@Singleton
class ElearningFileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager,
) : ElearningFileRepository {

    // One mutex per cache key so concurrent requests for the same file share a single
    // download, while different files still download in parallel.
    private val downloadLocks = ConcurrentHashMap<String, Mutex>()

    override suspend fun downloadFile(fileUrl: String, fileName: String): String {
        val key = cacheKey(fileUrl)
        val file = File(context.cacheDir, "$FILES_DIR/$key/${sanitize(fileName)}")
        downloadLocks.computeIfAbsent(key) { Mutex() }.withLock {
            if (file.exists() && file.length() > 0) return file.absolutePath

            file.parentFile?.mkdirs()
            val (api, token) = sessionManager.elearning()
            val channel = api.files.downloadFile(token, fileUrl)
            // Stream to a sibling temp file first so a death mid-download never leaves a
            // truncated file that the exists() check above would treat as a cache hit.
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

    // Keyed on the URL (not the name) so same-named files from different modules never
    // collide; the original file name is kept as the leaf so renderers and external
    // apps see the right extension.
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
