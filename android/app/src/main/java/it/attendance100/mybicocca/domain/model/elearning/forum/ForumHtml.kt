package it.attendance100.mybicocca.domain.model.elearning.forum

/**
 * Conversions between the plain text a composer field edits and the HTML body Moodle stores.
 * The mod_forum write services treat the message as FORMAT_HTML, so plain user input is promoted
 * to HTML (escaped, line breaks preserved) before sending, and an existing HTML post is
 * flattened back to editable plain text when pre-filling the edit composer.
 */
object ForumHtml {

    private val BlockBreak = Regex("(?i)<\\s*br\\s*/?>|</\\s*(p|div|li)\\s*>")
    private val Tag = Regex("<[^>]*>")
    private val Entities = mapOf(
        "&nbsp;" to " ", "&amp;" to "&", "&lt;" to "<", "&gt;" to ">",
        "&quot;" to "\"", "&#39;" to "'", "&apos;" to "'",
    )

    /** Escapes HTML metacharacters and turns line breaks into `<br>` for a FORMAT_HTML body. */
    fun plainToHtml(plain: String): String {
        val escaped = plain
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
        return escaped.replace("\r\n", "\n").replace("\n", "<br>")
    }

    /**
     * Reduces an HTML post body to plain text: `<br>` and block-element ends become newlines,
     * remaining tags are dropped, and common entities are decoded.
     */
    fun htmlToPlain(html: String): String {
        var text = html.replace(BlockBreak, "\n")
        text = text.replace(Tag, "")
        for ((entity, char) in Entities) text = text.replace(entity, char)
        return text.replace("\r\n", "\n")
            .lineSequence()
            .map { it.trim() }
            .joinToString("\n")
            .trim()
    }
}
