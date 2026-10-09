package it.attendance100.mybicocca.demo.repository

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import dagger.hilt.android.qualifiers.ApplicationContext
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
            writePlaceholderPdf(file, fileName.substringBeforeLast('.').replace('_', ' '))
        }
        file.absolutePath
    }

    override suspend fun authenticatedFileUrl(fileUrl: String): String = fileUrl

    private fun writePlaceholderPdf(file: File, title: String) {
        val document = PdfDocument()
        val heading = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val body = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 16f }
        repeat(PAGES) { index ->
            val page = document.startPage(PdfDocument.PageInfo.Builder(842, 595, index + 1).create())
            page.canvas.drawText(title.replaceFirstChar(Char::uppercase), 60f, 120f, heading)
            page.canvas.drawText("Materiale dimostrativo, pagina ${index + 1} di $PAGES", 60f, 170f, body)
            document.finishPage(page)
        }
        file.outputStream().use(document::writeTo)
        document.close()
    }

    private companion object {
        const val PAGES = 6
    }
}
