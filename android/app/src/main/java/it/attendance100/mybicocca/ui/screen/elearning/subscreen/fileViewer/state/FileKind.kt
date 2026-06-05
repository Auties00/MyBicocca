package it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.state

import java.util.Locale

// Which Microsoft Office app owns a document family. The ms-* protocol handlers are
// documented at learn.microsoft.com ("Integrate with Office from Android applications"):
// "<protocol>:ofv|u|<url>" opens the URL in forced view-only mode.
enum class OfficeApp(
    val packageName: String,
    val protocol: String,
    val label: String,
) {
    Word("com.microsoft.office.word", "ms-word", "Word"),
    Excel("com.microsoft.office.excel", "ms-excel", "Excel"),
    PowerPoint("com.microsoft.office.powerpoint", "ms-powerpoint", "PowerPoint"),
}

// How the viewer renders a file. Classification prefers the Moodle-reported mimetype but
// falls back to the extension — the survey showed a few entries with missing or wrong
// mimetypes (e.g. .py as document/unknown).
sealed interface FileKind {
    data object Pdf : FileKind
    data object Image : FileKind
    data object Video : FileKind
    data object Audio : FileKind
    data object Html : FileKind
    data object Text : FileKind
    data object Zip : FileKind
    data class Office(val app: OfficeApp) : FileKind
    data object Unknown : FileKind

    companion object {
        fun classify(fileName: String?, mimeType: String?): FileKind {
            val mime = mimeType.orEmpty().lowercase(Locale.ROOT)
            val ext = fileName.orEmpty().substringAfterLast('.', "").lowercase(Locale.ROOT)
            return when {
                mime == "application/pdf" || ext == "pdf" -> Pdf

                mime.contains("wordprocessingml") || mime.contains("msword") ||
                    ext in WORD_EXTS -> Office(OfficeApp.Word)
                mime.contains("spreadsheetml") || mime.contains("ms-excel") ||
                    ext in EXCEL_EXTS -> Office(OfficeApp.Excel)
                mime.contains("presentationml") || mime.contains("ms-powerpoint") ||
                    ext in POWERPOINT_EXTS -> Office(OfficeApp.PowerPoint)

                mime == "application/zip" || mime == "application/java-archive" ||
                    ext in ZIP_EXTS -> Zip

                mime.startsWith("image/") || ext in IMAGE_EXTS -> Image
                mime.startsWith("video/") || ext in VIDEO_EXTS -> Video
                mime.startsWith("audio/") || ext in AUDIO_EXTS -> Audio

                mime == "text/html" || ext == "html" || ext == "htm" -> Html

                mime.startsWith("text/") || mime == "application/json" ||
                    mime == "text/xml" || ext in TEXT_EXTS -> Text

                else -> Unknown
            }
        }

        private val WORD_EXTS = setOf("doc", "docx", "rtf", "odt")
        private val EXCEL_EXTS = setOf("xls", "xlsx", "ods")
        private val POWERPOINT_EXTS = setOf("ppt", "pptx", "pps", "ppsx", "odp")
        private val ZIP_EXTS = setOf("zip", "jar")
        private val IMAGE_EXTS = setOf("png", "jpg", "jpeg", "webp", "gif", "svg", "bmp")
        private val VIDEO_EXTS = setOf("mp4", "webm", "mkv", "mov", "m4v", "3gp")
        private val AUDIO_EXTS = setOf("mp3", "m4a", "wav", "ogg", "flac", "aac")
        // Source/code extensions seen in the course survey plus common ones; everything
        // here renders through the code viewer (plain monospace when no grammar exists).
        private val TEXT_EXTS = setOf(
            "txt", "md", "csv", "json", "xml", "yaml", "yml", "log",
            "java", "kt", "py", "ipynb", "sql", "c", "cpp", "h", "hpp", "cc", "cs",
            "js", "ts", "sh", "bat", "asm", "s", "m", "pl", "rb", "go", "rs",
            "lark", "circ", "dot", "tex", "properties", "gradle", "toml", "ini",
        )
    }
}
