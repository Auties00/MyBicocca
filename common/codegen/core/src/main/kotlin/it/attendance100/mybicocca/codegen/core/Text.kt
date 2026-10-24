package it.attendance100.mybicocca.codegen.core

/** Text helpers shared by every target. */
object Text {
    private val NEWLINE_WITH_SPACES = Regex("""\s*\n\s*""")
    private val WORD_BOUNDARY = Regex("""(?<=[a-z0-9])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])|[^A-Za-z0-9]+""")

    /** Joins the lines of [text] with single spaces, for one-line documentation. */
    fun singleLine(text: String): String = text.replace(NEWLINE_WITH_SPACES, " ").trim()

    /** Splits an identifier in any casing (`fooBar`, `FOO_BAR`, `HTTPServer`) into its words. */
    fun words(identifier: String): List<String> = identifier.split(WORD_BOUNDARY).filter { it.isNotEmpty() }

    /**
     * Converts an identifier in any casing to lowerCamelCase, normalising acronyms:
     * `InCorso` → `inCorso`, `AUTHENTICATED_USER` → `authenticatedUser`, `URLValue` → `urlValue`.
     */
    fun lowerCamel(identifier: String): String =
        words(identifier).mapIndexed { index, word ->
            if (index == 0) word.lowercase() else word.lowercase().capitalized()
        }.joinToString("")
}

fun String.capitalized(): String = replaceFirstChar { it.uppercaseChar() }

fun String.decapitalized(): String = replaceFirstChar { it.lowercaseChar() }

/** `fooBar` → `FOO_BAR`. */
fun String.toScreamingSnakeCase(): String = Text.words(this).joinToString("_") { it.uppercase() }
