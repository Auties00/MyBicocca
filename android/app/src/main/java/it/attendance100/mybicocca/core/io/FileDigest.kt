package it.attendance100.mybicocca.core.io

import java.io.File
import java.security.MessageDigest

/**
 * Lowercase-hex SHA-256 of this file's contents, streamed in fixed-size chunks so a large file
 * (e.g. a downloaded APK) never needs to be held in memory all at once. Returns null when the
 * file can't be read.
 */
fun File.sha256Hex(): String? = runCatching {
    val digest = MessageDigest.getInstance("SHA-256")
    inputStream().use { stream ->
        val buffer = ByteArray(DIGEST_BUFFER_BYTES)
        while (true) {
            val read = stream.read(buffer)
            if (read < 0) break
            digest.update(buffer, 0, read)
        }
    }
    digest.digest().joinToString("") { byte ->
        val int = byte.toInt() and 0xFF
        if (int < 16) "0" + int.toString(16) else int.toString(16)
    }
}.getOrNull()

private const val DIGEST_BUFFER_BYTES = 8 * 1024
