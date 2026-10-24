package it.attendance100.mybicocca.codegen.target.kotlin

import it.attendance100.mybicocca.codegen.core.CodeWriter
import it.attendance100.mybicocca.codegen.core.Text

/** Kotlin lexical rules: reserved words, literals and comments. */
internal object KotlinSyntax {
    /** Hard keywords, which can't be used as identifiers without backticks. */
    private val HARD_KEYWORDS = setOf(
        "as", "break", "class", "continue", "do", "else", "false", "for", "fun", "if", "in",
        "interface", "is", "null", "object", "package", "return", "super", "this", "throw",
        "true", "try", "typealias", "typeof", "val", "var", "when", "while",
    )

    fun identifier(name: String): String = if (name in HARD_KEYWORDS) "`$name`" else name

    /** A string literal's contents, with quotes, backslashes and `$` escaped. */
    fun stringContent(value: String): String = buildString {
        for (char in value) {
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '$' -> append("\\$")
                '\n' -> append("\\n")
                else -> append(char)
            }
        }
    }

    fun string(value: String): String = "\"${stringContent(value)}\""

    /** Text safe inside a KDoc comment, on one line. */
    fun docText(text: String): String = Text.singleLine(text).replace("*/", "*&#47;")

    /** Writes a one-line KDoc for [text], if any. */
    fun CodeWriter.doc(text: String?) {
        if (text != null) line("/** ${docText(text)} */")
    }
}
