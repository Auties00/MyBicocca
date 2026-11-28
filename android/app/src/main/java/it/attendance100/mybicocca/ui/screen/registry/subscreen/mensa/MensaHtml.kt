package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.buildAnnotatedString

/** Text with the bold and italic ranges the canteen's HTML carried; every other tag is dropped. */
internal data class MensaRichText(
    val text: String,
    val bold: List<IntRange> = emptyList(),
    val italic: List<IntRange> = emptyList(),
) {
    fun toAnnotatedString(): AnnotatedString = buildAnnotatedString {
        append(text)
        bold.forEach { addStyle(SpanStyle(fontWeight = FontWeight.Bold), it.first, it.last + 1) }
        italic.forEach { addStyle(SpanStyle(fontStyle = FontStyle.Italic), it.first, it.last + 1) }
    }
}

/**
 * The canteen API sends dish descriptions, ingredients and allergen names as small HTML
 * fragments (`<p>`, `<strong>`, `<span>`, `<br>`). This reads just those: tags are matched only
 * when `<` is followed by a letter or `/`, so a bare `>` or `<` in "Solfiti >10 mg/l" stays text.
 */
internal object MensaHtml {

    private val TAG = Regex("</?([a-zA-Z][a-zA-Z0-9]*)[^>]*?/?>")
    private val WHITESPACE = Regex("\\s+")
    private val BLOCKS = setOf("p", "div", "li", "ul", "ol", "tr", "h1", "h2", "h3", "h4")

    fun parse(html: String?): MensaRichText {
        if (html.isNullOrBlank()) return MensaRichText("")
        val out = StringBuilder()
        val bold = mutableListOf<IntRange>()
        val italic = mutableListOf<IntRange>()
        var boldStart = -1
        var italicStart = -1
        var boldDepth = 0
        var italicDepth = 0

        fun appendText(raw: String) {
            val decoded = decode(raw)
            if (decoded.isEmpty()) return
            val collapsed = WHITESPACE.replace(decoded, " ")
            val lead = out.isEmpty() || out.last() == ' ' || out.last() == '\n'
            out.append(if (lead) collapsed.trimStart() else collapsed)
        }

        fun lineBreak(double: Boolean) {
            while (out.isNotEmpty() && out.last() == ' ') out.setLength(out.length - 1)
            if (out.isEmpty()) return
            val wanted = if (double) 2 else 1
            val existing = out.takeLastWhile { it == '\n' }.length
            repeat((wanted - existing).coerceAtLeast(0)) { out.append('\n') }
        }

        var cursor = 0
        for (match in TAG.findAll(html)) {
            appendText(html.substring(cursor, match.range.first))
            cursor = match.range.last + 1
            val name = match.groupValues[1].lowercase()
            val closing = match.value.startsWith("</")
            when {
                name == "br" -> lineBreak(double = false)
                name in BLOCKS -> lineBreak(double = closing && name == "p")
                name == "b" || name == "strong" -> {
                    if (!closing) {
                        if (boldDepth++ == 0) boldStart = out.length
                    } else if (boldDepth > 0 && --boldDepth == 0 && out.length > boldStart) {
                        bold += boldStart until out.length
                    }
                }
                name == "i" || name == "em" -> {
                    if (!closing) {
                        if (italicDepth++ == 0) italicStart = out.length
                    } else if (italicDepth > 0 && --italicDepth == 0 && out.length > italicStart) {
                        italic += italicStart until out.length
                    }
                }
            }
        }
        appendText(html.substring(cursor))

        // Trailing blanks go; ranges are clamped to the text that is left.
        while (out.isNotEmpty() && (out.last() == ' ' || out.last() == '\n')) out.setLength(out.length - 1)
        val length = out.length
        fun List<IntRange>.clamped() = mapNotNull { r ->
            val last = minOf(r.last, length - 1)
            if (r.first <= last) r.first..last else null
        }
        return MensaRichText(out.toString(), bold.clamped(), italic.clamped())
    }

    /** The text alone, on one line: breaks become spaces. */
    fun plain(html: String?): String = WHITESPACE.replace(parse(html).text, " ").trim()

    private val NAMED = mapOf(
        "amp" to "&", "lt" to "<", "gt" to ">", "quot" to "\"", "apos" to "'", "nbsp" to " ",
        "agrave" to "à", "egrave" to "è", "eacute" to "é", "igrave" to "ì", "ograve" to "ò", "ugrave" to "ù",
        "Egrave" to "È", "Agrave" to "À", "deg" to "°", "euro" to "€",
    )

    private val ENTITY = Regex("&(#x[0-9a-fA-F]+|#[0-9]+|[a-zA-Z]+);")

    private fun decode(text: String): String {
        if ('&' !in text) return text
        return ENTITY.replace(text) { match ->
            val body = match.groupValues[1]
            val replacement = when {
                body.startsWith("#x") -> body.drop(2).toIntOrNull(16)?.let { String(Character.toChars(it)) }
                body.startsWith("#") -> body.drop(1).toIntOrNull()?.let { String(Character.toChars(it)) }
                else -> NAMED[body]
            }
            replacement ?: match.value
        }
    }
}

/**
 * Cleans what the canteen publishes as a description. Most dishes carry the same generic blurb
 * about the chefs and the HACCP plan, which says nothing about the dish, so it counts as no
 * description. A dish whose text differs from it has a real one, which is shown cleaned up.
 */
internal object MensaDescriptions {

    /** The generic text as the API sends it, kept to compare against. Last seen 2026-11-19. */
    private const val GENERIC =
        "<p>I nostri Chef, nell'elaborare le ricette utilizzeranno prodotti di prima qualità e si " +
            "atterranno scrupolosamente alle procedure dettate  dal ricettario aziendale. Tali procedure " +
            "sono frutto dell'esperienza maturata in molti anni di attività.</p>\r\n<p><strong></strong>N.B. : " +
            "Gli allergeni indicati derivano dagli ingredienti utilizzati, dalle ricette operative e dalle " +
            "schede tecniche dei prodotti. La preparazione e il servizio saranno gestiti secondo le procedure " +
            "previste dal piano HACCP, con misure atte a prevenire e ridurre il rischio di contaminazioni " +
            "crociate. Ingredienti e allergeni saranno comunicati agli utenti secondo la normativa vigente.</p>" +
            "\r\n<p><strong></strong></p>"

    /** Short placeholders some ready-made products carry instead of a description. */
    private val PLACEHOLDERS = listOf("Prodotto pronto")

    private val GENERIC_KEYS = (listOf(GENERIC) + PLACEHOLDERS).map(::key).toSet()

    fun useful(raw: String?): MensaRichText? {
        if (raw.isNullOrBlank()) return null
        val key = key(raw)
        // The API has been seen to corrupt accents into U+FFFD, so the comparison ignores them.
        if (key.isEmpty() || key in GENERIC_KEYS) return null
        return MensaHtml.parse(raw).takeIf { it.text.isNotBlank() }
    }

    private fun key(html: String): String =
        MensaHtml.plain(html).lowercase().filter { it.isLetterOrDigit() && it != '�' && it.code < 0x80 }
}

/** An allergen name as a chip label: no HTML, no leading "Contiene", first letter upper case. */
internal fun cleanAllergenName(raw: String): String {
    val plain = MensaHtml.plain(raw)
    val stripped = plain.replace(Regex("^contiene\\s+", RegexOption.IGNORE_CASE), "")
    return stripped.replaceFirstChar { it.uppercase() }
}
