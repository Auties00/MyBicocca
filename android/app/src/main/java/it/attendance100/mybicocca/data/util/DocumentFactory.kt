package it.attendance100.mybicocca.data.util

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import it.attendance100.mybicocca.data.model.document.AppDocument
import kotlinx.io.readByteArray

suspend fun ByteReadChannel.toAppDocument(
    fileName: String,
    mimeType: String = "application/pdf",
): AppDocument = AppDocument(
    fileName = fileName,
    mimeType = mimeType,
    bytes = readRemaining().readByteArray(),
)
