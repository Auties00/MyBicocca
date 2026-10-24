package it.attendance100.mybicocca.codegen.target.swift

import it.attendance100.mybicocca.codegen.core.CodeWriter
import it.attendance100.mybicocca.codegen.core.Text

/** Swift lexical rules: reserved words, literals and comments. */
internal object SwiftSyntax {
    private val KEYWORDS = setOf(
        "associatedtype", "class", "deinit", "enum", "extension", "fileprivate", "func", "import",
        "init", "inout", "internal", "let", "open", "operator", "private", "precedencegroup",
        "protocol", "public", "rethrows", "static", "struct", "subscript", "typealias", "var",
        "break", "case", "catch", "continue", "default", "defer", "do", "else", "fallthrough",
        "for", "guard", "if", "in", "repeat", "return", "throw", "switch", "where", "while",
        "Any", "as", "await", "false", "is", "nil", "self", "Self", "super", "throws", "true",
        "try", "Type", "Protocol",
    )

    /** [name] as an identifier: reserved words get backticks, a leading digit an underscore. */
    fun identifier(name: String): String = when {
        name.first().isDigit() -> "_$name"
        name in KEYWORDS -> "`$name`"
        else -> name
    }

    fun string(value: String): String = buildString {
        append('"')
        for (char in value) {
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                else -> append(char)
            }
        }
        append('"')
    }

    fun docText(text: String): String = Text.singleLine(text)

    fun CodeWriter.doc(text: String?) {
        if (text != null) line("/// ${docText(text)}")
    }
}
