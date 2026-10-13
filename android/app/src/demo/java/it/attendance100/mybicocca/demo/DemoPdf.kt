package it.attendance100.mybicocca.demo

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.ByteArrayOutputStream

/** Placeholder PDFs for everything the demo build would otherwise download or have generated. */
object DemoPdf {

    private const val A4_SHORT = 595
    private const val A4_LONG = 842

    /** An official-looking single page: the student's details, then [lines]. */
    fun certificate(title: String, lines: List<String>): ByteArray = render(A4_SHORT, A4_LONG, pages = 1) { canvas, _ ->
        val body = text(12f)
        canvas.drawText("Università degli Studi di Milano-Bicocca", 48f, 64f, body)
        canvas.drawText(title, 48f, 104f, text(20f, bold = true))
        val student = "${DemoStudent.FULL_NAME} · matricola ${DemoStudent.STUDENT_NUMBER}"
        (listOf(student, "") + lines + listOf("", "Documento dimostrativo, privo di valore."))
            .forEachIndexed { index, line -> canvas.drawText(line, 48f, 144f + index * 20f, body) }
    }

    /** A landscape deck of [pages] slides, each carrying [title], standing in for course material. */
    fun slides(title: String, pages: Int): ByteArray = render(A4_LONG, A4_SHORT, pages) { canvas, page ->
        canvas.drawText(title, 60f, 120f, text(34f, bold = true))
        canvas.drawText("Materiale dimostrativo, pagina ${page + 1} di $pages", 60f, 170f, text(16f))
    }

    private fun render(width: Int, height: Int, pages: Int, draw: (Canvas, Int) -> Unit): ByteArray {
        val document = PdfDocument()
        repeat(pages) { index ->
            val page = document.startPage(PdfDocument.PageInfo.Builder(width, height, index + 1).create())
            draw(page.canvas, index)
            document.finishPage(page)
        }
        return ByteArrayOutputStream().also { document.writeTo(it); document.close() }.toByteArray()
    }

    private fun text(size: Float, bold: Boolean = false) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        if (bold) typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
}
