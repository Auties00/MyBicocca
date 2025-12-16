package it.attendance100.mybicocca.util

import java.util.Locale

private const val MAX_INPUT_LENGTH = 10_000

private fun removeWhitespace(input: String): String {
    return input.replace("\\s".toRegex(), "")
}

fun String.cleaned(): String {
    return removeWhitespace(this)
}

private fun toTitleCase(input: String?): String {
    if (input.isNullOrEmpty()) return ""

    require(input.length <= MAX_INPUT_LENGTH) {
        "Input exceeds maximum allowed length of $MAX_INPUT_LENGTH characters."
    }

    val safeInput = input.trim()

    val words = safeInput.split("\\s+".toRegex()).toTypedArray()

    val builder = StringBuilder(safeInput.length)
    for (word in words) {
        if (word.isEmpty()) continue
        if (builder.isNotEmpty()) builder.append(" ")

        // Always enforce a deterministic Locale
        val firstChar = word.take(1).uppercase(Locale.ROOT)
        val rest = word.substring(1).lowercase(Locale.ROOT)

        builder.append(firstChar).append(rest)
    }

    return builder.toString()
}

fun String.titleCase(): String {
    return toTitleCase(this)
}