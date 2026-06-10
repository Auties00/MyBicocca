package it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli.ext

import it.attendance100.mybicocca.domain.model.exam.BookedExam
import it.attendance100.mybicocca.domain.model.exam.ExamCallType
import it.attendance100.mybicocca.domain.model.exam.ExamGrade
import it.attendance100.mybicocca.domain.model.exam.ExamType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Esse3 stores titles in ALL CAPS ("METODI ALGEBRICI PER L'INFORMATICA") and writes
 * accented finals as a trailing apostrophe ("AFFIDABILITA'"). Renders them as Italian
 * title case ("Metodi Algebrici per l'Informatica", "Affidabilità").
 */
fun BookedExam.displayTitle(): String =
    activityDescription?.takeIf { it.isNotBlank() }?.let { italianTitleCase(it) } ?: "Esame"

/**
 * Subtitle for a sat (Passate) booking: when the appello was taken, with a hint while the
 * outcome is still missing. "Sostenuto il 24 feb 2026" / "Sostenuto il 24 feb 2026 · in
 * attesa di esito".
 */
fun BookedExam.sittingLabel(): String? {
    val date = examDateTime?.toLocalDate() ?: return null
    val sat = "Sostenuto il ${date.format(SittingDateFormat)}"
    return if (outcomePublished || grade != ExamGrade.Unknown) sat else "$sat · in attesa di esito"
}

private val SittingDateFormat = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ITALIAN)

/** Relative countdown to the appello: "Oggi", "Domani", "Tra N giorni" up to 60 days out, else null. */
fun BookedExam.countdownLabel(today: LocalDate): String? {
    val examDate = examDateTime?.toLocalDate() ?: return null
    return when (val days = ChronoUnit.DAYS.between(today, examDate)) {
        0L -> "Oggi"
        1L -> "Domani"
        in 2..60 -> "Tra $days giorni"
        else -> null
    }
}

/**
 * Card subtitle: the exam mode, flagged when the call is a partial ("Scritto (Parziale)").
 * A partial with no known mode still gets "Prova parziale".
 */
fun BookedExam.examKindLabel(): String? {
    val base = examType.displayLabel()
        ?: return if (callType == ExamCallType.Partial) "Prova parziale" else null
    return if (callType == ExamCallType.Partial) "$base (Parziale)" else base
}

/** Italian label for the exam mode; null when unknown. */
fun ExamType.displayLabel(): String? = when (this) {
    ExamType.Written -> "Scritto"
    ExamType.Oral -> "Orale"
    ExamType.WrittenAndOralJoint, ExamType.WrittenAndOralSeparate -> "Scritto e Orale"
    ExamType.Unknown -> null
}

/**
 * "Prova parziale" badge from tipoAppCod — finals are the norm, so only partials get
 * flagged. Suppressed when the desAppello qualifier from [examVariantLabel] already says it.
 */
fun BookedExam.partialLabel(): String? {
    if (callType != ExamCallType.Partial) return null
    val variant = examVariantLabel()?.lowercase(Locale.ITALIAN)
    if (variant != null && ("parzial" in variant || "intermed" in variant)) return null
    return "Prova parziale"
}

/**
 * "N CFU" weight label. Esse3's pesoAd carries two optional decimals but is an integer for
 * every real course, so whole values drop the fraction.
 */
fun BookedExam.creditsLabel(): String? = credits?.takeIf { it > 0 }?.let {
    if (it % 1f == 0f) "${it.toInt()} CFU" else "$it CFU"
}

/**
 * The real qualifier carried by Esse3's desAppello, or null when the description adds
 * nothing. desAppello usually just repeats the course name, sometimes truncated at 40
 * chars or with a typo, and only occasionally carries a real qualifier. Live examples:
 * ```
 * "MATEMATICA II - prova parziale"            -> "Prova parziale"
 * "BASI DI DATI - T1"                         -> "T1"
 * "SICUREZZA ED AFFIDAB-scritto completo"     -> "Scritto completo"  (truncated title)
 * "PROBABILITA' E STATISTICA PER L'INFORMAT"  -> null                (truncated dup)
 * "ALGORITMI E STRUTTRE DATI"                 -> null                (typo'd dup)
 * "seconda prova intermedia"                  -> "Seconda prova intermedia"
 * ```
 * Description tokens are greedily aligned against title tokens (allowing skips in the
 * title and truncated tokens on either side), anchored on the title's first word — a
 * description that doesn't open with the course name ("pre-appello ...", "seconda prova
 * intermedia") is a freestanding label, not title + qualifier. The leftover tail is the
 * qualifier; a tail made entirely of near-misses of title words is a misspelled duplicate
 * and yields null.
 */
fun BookedExam.examVariantLabel(): String? {
    val description = examCallDescription?.trim()?.takeIf { it.isNotBlank() } ?: return null
    val title = activityDescription?.trim()?.takeIf { it.isNotBlank() }
        ?: return sentenceCase(description)

    val normalizedTitle = title.lowercase(Locale.ITALIAN)
    val normalizedDescription = description.lowercase(Locale.ITALIAN)
    if (normalizedDescription == normalizedTitle) return null
    if (normalizedTitle.startsWith(normalizedDescription)) return null

    val titleTokens = tokenize(title)
    val descriptionTokens = tokenize(description)

    var titleIndex = 0
    var consumed = 0
    for (token in descriptionTokens) {
        val searchRange = if (consumed == 0) 0..0 else titleIndex until titleTokens.size
        val match = searchRange.firstOrNull { i ->
            fuzzyTokensMatch(token, titleTokens[i])
        } ?: break
        titleIndex = match + 1
        consumed++
    }
    if (consumed == 0) return sentenceCase(description)

    val tail = descriptionTokens.drop(consumed)
    if (tail.isEmpty()) return null
    if (tail.all { t -> titleTokens.any { fuzzyTokensMatch(t, it) } }) return null
    return sentenceCase(tail.joinToString(" "))
}

/**
 * Room + building line. Rooms already embed the building code ("U24-C2" in "Edificio U24 -
 * Viale Sarca 336"), so showing both repeats it: when they match, the room is kept and the
 * building is reduced to its address.
 */
fun BookedExam.locationLabel(): String? {
    val room = classroomDescription?.trim()?.takeIf { it.isNotBlank() }
    val building = buildingDescription?.trim()?.takeIf { it.isNotBlank() }
    if (room == null) return building
    if (building == null) return room
    val withoutPrefix = building.removePrefix("Edificio ").removePrefix("edificio ").trim()
    val buildingCode = withoutPrefix.substringBefore(" -").trim()
    if (!room.startsWith(buildingCode, ignoreCase = true)) return "$room · $building"
    val address = withoutPrefix.substringAfter(" -", "").trim()
    return if (address.isEmpty()) room else "$room · $address"
}

private val MinorWords = setOf(
    "di", "a", "da", "in", "con", "su", "per", "tra", "fra", "e", "ed", "o", "od",
    "il", "lo", "la", "i", "gli", "le", "un", "uno", "una",
    "del", "dello", "della", "dei", "degli", "delle",
    "al", "allo", "alla", "ai", "agli", "alle",
    "dal", "dallo", "dalla", "dai", "dagli", "dalle",
    "nel", "nello", "nella", "nei", "negli", "nelle",
    "sul", "sullo", "sulla", "sui", "sugli", "sulle",
)

/** Multi-char only: single letters ("i", "v") are far likelier to be words than numerals. */
private val RomanNumerals = setOf("ii", "iii", "iv", "vi", "vii", "viii", "ix", "xi", "xii")

private val ElidedArticles = setOf("l", "un", "d", "dell", "all", "dall", "nell", "sull")

/**
 * Recases an ALL-CAPS Esse3 string as Italian title case: minor words stay lowercase past
 * the first position, roman numerals go uppercase, elided articles capitalize after the
 * apostrophe, and trailing-apostrophe finals regain their accent.
 */
fun italianTitleCase(raw: String): String {
    val cleaned = restoreFinalAccents(raw.trim().replace(Regex("\\s+"), " ").lowercase(Locale.ITALIAN))
    val words = cleaned.split(" ")
    return words.mapIndexed { index, word ->
        when {
            word in RomanNumerals -> word.uppercase(Locale.ITALIAN)
            index > 0 && word in MinorWords -> word
            else -> capitalizeWord(word, isFirst = index == 0)
        }
    }.joinToString(" ")
}

/**
 * Elided articles keep the article lowercase and capitalize after the apostrophe:
 * "l'informatica" -> "l'Informatica" ("L'Informatica" at the start of the title).
 */
private fun capitalizeWord(word: String, isFirst: Boolean): String {
    val apostrophe = word.indexOfFirst { it == '\'' || it == '’' }
    if (apostrophe > 0 && apostrophe < word.length - 1 &&
        word.take(apostrophe) in ElidedArticles
    ) {
        val article = if (isFirst) word.take(apostrophe).replaceFirstChar { it.titlecase(Locale.ITALIAN) } else word.take(apostrophe)
        return article + word[apostrophe] + word.drop(apostrophe + 1).replaceFirstChar { it.titlecase(Locale.ITALIAN) }
    }
    return word.replaceFirstChar { it.titlecase(Locale.ITALIAN) }
}

/**
 * "affidabilita'" -> "affidabilità". Only at word end, so elisions ("l'informatica")
 * are untouched.
 */
private fun restoreFinalAccents(text: String): String =
    Regex("([aeiou])['’](?=\\s|$)").replace(text) {
        when (it.groupValues[1]) {
            "a" -> "à"
            "e" -> "è"
            "i" -> "ì"
            "o" -> "ò"
            else -> "ù"
        }
    }

/** Sentence case that keeps codes like "T1" and roman numerals, demoting every other word past the first. */
private fun sentenceCase(text: String): String =
    italianTitleCase(text).split(" ").mapIndexed { index, word ->
        when {
            index == 0 -> word
            word.any { it.isDigit() } || word == word.uppercase(Locale.ITALIAN) -> word
            else -> word.lowercase(Locale.ITALIAN)
        }
    }.joinToString(" ")

/**
 * Apostrophes split too, so an elision ("l'informatica") aligns with the bare word
 * Esse3 truncates it to ("informat").
 */
private fun tokenize(text: String): List<String> =
    text.lowercase(Locale.ITALIAN).split(Regex("[\\s\\-–—'’]+")).filter { it.isNotBlank() }

/**
 * Truncated forms ("ris" vs "risorse", "informat" vs "informatica") count as the same
 * word, but only from 2 chars up to avoid spurious single-letter hits.
 */
private fun tokensMatch(a: String, b: String): Boolean = when {
    a == b -> true
    a.length >= 2 && b.startsWith(a) -> true
    b.length >= 2 && a.startsWith(b) -> true
    else -> false
}

/**
 * Adds typo tolerance ("struttre" vs "strutture") on words long enough that an edit
 * or two can't turn one real word into another ("t1" vs "di" must NOT match).
 */
private fun fuzzyTokensMatch(a: String, b: String): Boolean {
    if (tokensMatch(a, b)) return true
    if (a.length < 4 || b.length < 4) return false
    val maxEdits = if (minOf(a.length, b.length) >= 6) 2 else 1
    return editDistanceAtMost(a, b, maxEdits)
}

private fun editDistanceAtMost(a: String, b: String, max: Int): Boolean {
    if (kotlin.math.abs(a.length - b.length) > max) return false
    val previous = IntArray(b.length + 1) { it }
    val current = IntArray(b.length + 1)
    for (i in 1..a.length) {
        current[0] = i
        var rowMin = i
        for (j in 1..b.length) {
            val cost = if (a[i - 1] == b[j - 1]) 0 else 1
            current[j] = minOf(current[j - 1] + 1, previous[j] + 1, previous[j - 1] + cost)
            rowMin = minOf(rowMin, current[j])
        }
        if (rowMin > max) return false
        current.copyInto(previous)
    }
    return previous[b.length] <= max
}
