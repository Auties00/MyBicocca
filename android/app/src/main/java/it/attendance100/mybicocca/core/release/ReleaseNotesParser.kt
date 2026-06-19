package it.attendance100.mybicocca.core.release

/**
 * Parses a GitHub release body into [ReleaseNotes] blocks. Block-level only: headings, bullet
 * lists, GitHub alert callouts (`> [!TIP]` …) and everything else collapsed into paragraphs;
 * inline Markdown inside each block is left verbatim for the renderer to style.
 *
 * It is forgiving by design — release bodies in the wild range from the project's full template to
 * an empty string (the published 0.0.x tags ship no notes) to a couple of freehand lines. Anything
 * it does not recognise as a heading, list, or callout becomes paragraph text, and an empty body
 * yields empty [ReleaseNotes]. Blank lines separate blocks; consecutive prose lines join into one
 * paragraph (Markdown soft-wrap), and consecutive bullets gather into one list.
 */
fun parseReleaseNotes(body: String): ReleaseNotes {
    val lines = body.replace("\r\n", "\n").replace("\r", "\n").split("\n")
    val blocks = mutableListOf<ReleaseBlock>()

    val paragraph = mutableListOf<String>()
    val bullets = mutableListOf<BulletItem>()

    fun flushParagraph() {
        if (paragraph.isNotEmpty()) {
            blocks += ReleaseBlock.Paragraph(paragraph.joinToString(" ").trim())
            paragraph.clear()
        }
    }

    fun flushBullets() {
        if (bullets.isNotEmpty()) {
            blocks += ReleaseBlock.BulletList(bullets.toList())
            bullets.clear()
        }
    }

    fun flushAll() {
        flushBullets()
        flushParagraph()
    }

    var i = 0
    while (i < lines.size) {
        val raw = lines[i]
        val line = raw.trim()

        val heading = HEADING_REGEX.matchEntire(line)
        val calloutKind = CALLOUT_REGEX.matchEntire(line)?.groupValues?.get(1)?.let(::calloutKindOf)
        val bullet = BULLET_REGEX.matchEntire(line)

        when {
            line.isEmpty() -> flushAll()

            heading != null -> {
                flushAll()
                blocks += ReleaseBlock.Heading(
                    level = heading.groupValues[1].length,
                    text = heading.groupValues[2].trim(),
                )
            }

            calloutKind != null -> {
                flushAll()
                val bodyLines = mutableListOf<String>()
                i++
                while (i < lines.size && lines[i].trimStart().startsWith(">")) {
                    bodyLines += lines[i].trimStart().removePrefix(">").removePrefix(" ")
                    i++
                }
                blocks += ReleaseBlock.Callout(calloutKind, bodyLines.joinToString("\n").trim())
                continue // i already points past the callout body
            }

            bullet != null -> {
                flushParagraph()
                bullets += BulletItem(bullet.groupValues[1].trim())
            }

            else -> {
                flushBullets()
                // A non-alert blockquote still reads as prose once its marker is stripped.
                paragraph += line.removePrefix(">").trimStart()
            }
        }
        i++
    }
    flushAll()

    return ReleaseNotes(blocks)
}

private fun calloutKindOf(token: String): CalloutKind? =
    when (token.uppercase()) {
        "NOTE" -> CalloutKind.NOTE
        "TIP" -> CalloutKind.TIP
        "IMPORTANT" -> CalloutKind.IMPORTANT
        "WARNING" -> CalloutKind.WARNING
        "CAUTION" -> CalloutKind.CAUTION
        else -> null
    }

private val HEADING_REGEX = Regex("^(#{1,6})\\s+(.+?)\\s*#*$")
private val CALLOUT_REGEX =
    Regex("^>\\s*\\[!(NOTE|TIP|IMPORTANT|WARNING|CAUTION)]\\s*$", RegexOption.IGNORE_CASE)
private val BULLET_REGEX = Regex("^(?:[-*+]|\\d+\\.)\\s+(.+)$")
