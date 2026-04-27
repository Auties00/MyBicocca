package it.attendance100.mybicocca.util

import java.text.Normalizer

private val trailingParens = Regex("\\s*\\([^)]*\\)\\s*$")
private val nonAlphanumeric = Regex("[^\\p{L}\\p{Nd}]+")
private val whitespaceRun = Regex("\\s+")

/**
 * Normalize a course title for cross-system matching between Esse3 (adLibDes) and
 * EasyStaff (label). Drops trailing parentheticals like "(mod. A)" / "(NO LEZIONI)",
 * strips accents, collapses punctuation to spaces, and lowercases.
 */
fun String.normalizeCourseName(): String {
    val withoutTail = replace(trailingParens, "")
    val ascii = Normalizer.normalize(withoutTail, Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
    return ascii
        .lowercase()
        .replace(nonAlphanumeric, " ")
        .replace(whitespaceRun, " ")
        .trim()
}

/**
 * True when [subjectName] likely belongs to partition [partialCode] (e.g. "A-L", "M-Z").
 * Partition info is typically encoded in EasyStaff names as " (A-L)" or similar; we
 * match conservatively on a standalone token.
 */
fun matchesPartition(subjectName: String, partialCode: String): Boolean {
    val normalizedCode = partialCode.normalizeCourseName()
    if (normalizedCode.isEmpty()) return true
    val tokens = subjectName.normalizeCourseName().split(' ')
    return normalizedCode in tokens
}
