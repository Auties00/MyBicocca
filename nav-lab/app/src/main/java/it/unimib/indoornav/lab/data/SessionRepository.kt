package it.unimib.indoornav.lab.data

import android.content.Context
import it.unimib.indoornav.lab.format.QuickLookStatus
import it.unimib.indoornav.lab.format.SessionEvaluator
import it.unimib.indoornav.lab.format.SessionHealthReport
import it.unimib.indoornav.lab.format.SessionManifest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class SessionItem(
    val dir: File,
    val folderName: String,
    val manifest: SessionManifest?,
    val health: SessionHealthReport,
    val lastModifiedMs: Long,
)

class SessionRepository(private val context: Context) {
    fun sessionsRootDir(): File {
        val base = context.getExternalFilesDir(null) ?: context.filesDir
        return File(base, "sessions").apply { mkdirs() }
    }

    fun surveyDir(): File {
        return File(sessionsRootDir(), "survey").apply { mkdirs() }
    }

    suspend fun listSessions(): List<SessionItem> = withContext(Dispatchers.IO) {
        val root = sessionsRootDir()
        val dirs = root.listFiles { f -> f.isDirectory && f.name != "survey" } ?: emptyArray()
        dirs.sortedByDescending { it.lastModified() }.map { dir ->
            val manifestFile = File(dir, "manifest.json")
            val manifest = if (manifestFile.exists()) {
                runCatching { SessionManifest.fromJsonString(manifestFile.readText()) }.getOrNull()
            } else {
                null
            }
            val health = runCatching { SessionEvaluator.evaluateDirectory(dir, manifest) }
                .getOrElse {
                    SessionHealthReport(
                        status = QuickLookStatus.RED,
                        durationSec = 0.0,
                        sizeBytes = 0L,
                        streamRows = emptyMap(),
                        findings = listOf("Failed to inspect session: ${it.message}"),
                    )
                }
            SessionItem(
                dir = dir,
                folderName = dir.name,
                manifest = manifest,
                health = health,
                lastModifiedMs = dir.lastModified(),
            )
        }
    }

    suspend fun listSurveyFiles(): List<File> = withContext(Dispatchers.IO) {
        surveyDir().listFiles { f -> f.isFile }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    suspend fun deleteSession(dir: File): Boolean = withContext(Dispatchers.IO) {
        dir.deleteRecursively()
    }

    suspend fun zipSession(sessionDir: File): File = withContext(Dispatchers.IO) {
        val zipCacheDir = File(context.cacheDir, "zips").apply { mkdirs() }
        val zipFile = File(zipCacheDir, "${sessionDir.name}.zip")
        if (zipFile.exists()) zipFile.delete()

        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
            val files = sessionDir.listFiles()?.filter { it.isFile }?.sortedBy { it.name } ?: emptyList()
            for (file in files) {
                val entry = ZipEntry("${sessionDir.name}/${file.name}")
                zos.putNextEntry(entry)
                file.inputStream().use { input -> input.copyTo(zos) }
                zos.closeEntry()
            }
        }
        zipFile
    }
}
