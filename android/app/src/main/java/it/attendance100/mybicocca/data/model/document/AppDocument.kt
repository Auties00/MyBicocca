package it.attendance100.mybicocca.data.model.document

data class AppDocument(
    val fileName: String,
    val mimeType: String = "application/pdf",
    val bytes: ByteArray,
)
