package it.attendance100.mybicocca.codegen.core

/**
 * Builds source text line by line, keeping track of indentation, so emitters describe
 * structure (blocks) rather than counting spaces.
 *
 * ```
 * writer.block("class Foo {") {
 *     line("val bar = 1")
 * }
 * ```
 */
class CodeWriter(private val indentUnit: String = "    ") {
    private val builder = StringBuilder()
    private var depth = 0

    /** Writes [text] as one line at the current indentation; blank lines carry no indentation. */
    fun line(text: String = ""): CodeWriter = apply {
        if (text.isEmpty()) {
            builder.append('\n')
        } else {
            repeat(depth) { builder.append(indentUnit) }
            builder.append(text).append('\n')
        }
    }

    /** Writes each line of a multi-line [text] at the current indentation. */
    fun lines(text: String): CodeWriter = apply { text.lines().forEach(::line) }

    /** Runs [body] one indentation level deeper. */
    fun indent(body: CodeWriter.() -> Unit): CodeWriter = apply {
        depth++
        try {
            body()
        } finally {
            depth--
        }
    }

    /** Writes [opener], then [body] one level deeper, then [closer]. */
    fun block(opener: String, closer: String = "}", body: CodeWriter.() -> Unit): CodeWriter = apply {
        line(opener)
        indent(body)
        line(closer)
    }

    /**
     * Writes [items] one per line between [opener] and [closer], separated by commas, as in a
     * wrapped parameter or argument list. With [trailingComma] the last item gets one too.
     * Items may span several lines (see [nested]).
     */
    fun commaList(opener: String, items: List<String>, closer: String, trailingComma: Boolean = false): CodeWriter = apply {
        line(opener)
        indent {
            items.forEachIndexed { index, item ->
                lines(item + if (index < items.lastIndex || trailingComma) "," else "")
            }
        }
        line(closer)
    }

    /**
     * Like [commaList], but writes everything on one line when it fits within [maxLength]
     * characters (indentation excluded) and no item spans several lines.
     */
    fun commaListOrLine(opener: String, items: List<String>, closer: String, maxLength: Int, trailingComma: Boolean = false): CodeWriter = apply {
        val single = items.joinToString(", ", opener, closer)
        if (items.none { '\n' in it } && single.length <= maxLength) line(single) else commaList(opener, items, closer, trailingComma)
    }

    /**
     * Renders [body] on its own, indented relative to column zero, so it can be embedded as
     * one multi-line item of [commaList] or [lines].
     */
    fun nested(body: CodeWriter.() -> Unit): String = CodeWriter(indentUnit).apply(body).toString().trimEnd('\n')

    /**
     * Writes [items] with [render], separated by [separator] lines (blank by default), which
     * is how declarations and members are spaced in every target language.
     */
    fun <T> separated(items: Iterable<T>, separator: () -> Unit = { line() }, render: CodeWriter.(T) -> Unit): CodeWriter = apply {
        items.forEachIndexed { index, item ->
            if (index > 0) separator()
            render(item)
        }
    }

    override fun toString(): String = builder.toString()
}
