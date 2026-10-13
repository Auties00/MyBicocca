package it.attendance100.mybicocca.demo.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import it.attendance100.mybicocca.demo.DemoPdf
import it.attendance100.mybicocca.domain.repository.ElearningFileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** Every "download" is a small PDF generated on the device and titled after the requested file. */
@Singleton
class DemoElearningFileRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : ElearningFileRepository {

    override suspend fun downloadFile(fileUrl: String, fileName: String): String = withContext(Dispatchers.IO) {
        val file = File(context.cacheDir, "demo_files/${fileUrl.hashCode().toUInt()}/$fileName")
        if (!file.exists()) {
            file.parentFile?.mkdirs()
            val title = fileName.substringBeforeLast('.').replace('_', ' ').replaceFirstChar(Char::uppercase)
            file.writeBytes(DemoPdf.slides(title, PAGES))
        }
        file.absolutePath
    }

    override suspend fun authenticatedFileUrl(fileUrl: String): String = fileUrl

    private companion object {
        const val PAGES = 6
    }
}
