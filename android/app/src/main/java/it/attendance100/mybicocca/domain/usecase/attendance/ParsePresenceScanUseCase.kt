package it.attendance100.mybicocca.domain.usecase.attendance

import it.attendance100.mybicocca.domain.model.attendance.PresenceScan
import javax.inject.Inject

class ParsePresenceScanUseCase @Inject constructor() {

    operator fun invoke(raw: String): PresenceScan {
        val text = raw.trim()
        if (text.isEmpty()) return PresenceScan.Unrecognized(raw)

        if (text.contains(ATTENDANCE_SESSION_PATH, ignoreCase = true)) {
            val params = queryParams(text)
            val sessionId = params["sessid"]
            return if (sessionId != null) {
                PresenceScan.SessionLink(sessionId, params["qrpass"])
            } else {
                PresenceScan.Unrecognized(raw)
            }
        }

        if (text.startsWith("http://", ignoreCase = true) || text.startsWith("https://", ignoreCase = true)) {
            val params = queryParams(text)
            val code = params["codice_lezione"] ?: params["codice"] ?: params["code"] ?: params["lezione"]
            return if (code != null) PresenceScan.LessonCode(code) else PresenceScan.Unrecognized(raw)
        }

        return PresenceScan.LessonCode(text)
    }

    private fun queryParams(url: String): Map<String, String> {
        val query = url.substringAfter('?', "")
        if (query.isEmpty()) return emptyMap()
        return query.split('&').mapNotNull { pair ->
            val separator = pair.indexOf('=')
            if (separator <= 0) return@mapNotNull null
            pair.substring(0, separator).lowercase() to decode(pair.substring(separator + 1))
        }.toMap()
    }

    private fun decode(value: String): String {
        if ('%' !in value && '+' !in value) return value
        val builder = StringBuilder(value.length)
        var index = 0
        while (index < value.length) {
            val char = value[index]
            when {
                char == '+' -> {
                    builder.append(' ')
                    index++
                }

                char == '%' && index + 2 < value.length -> {
                    val code = value.substring(index + 1, index + 3).toIntOrNull(16)
                    if (code != null) {
                        builder.append(code.toChar())
                        index += 3
                    } else {
                        builder.append(char)
                        index++
                    }
                }

                else -> {
                    builder.append(char)
                    index++
                }
            }
        }
        return builder.toString()
    }

    private companion object {
        const val ATTENDANCE_SESSION_PATH = "mod/attendance/attendance.php"
    }
}
