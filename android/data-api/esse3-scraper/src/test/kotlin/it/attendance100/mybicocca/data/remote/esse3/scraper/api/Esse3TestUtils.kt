package it.attendance100.mybicocca.data.remote.esse3.scraper.api

import io.ktor.utils.io.*

private val PDF_MAGIC = byteArrayOf(0x25, 0x50, 0x44, 0x46, 0x2D) // %PDF-

suspend fun ByteReadChannel.isPdf(): Boolean {
    val header = ByteArray(PDF_MAGIC.size)
    val bytesRead = readAvailable(header)
    return bytesRead == PDF_MAGIC.size && header.contentEquals(PDF_MAGIC)
}