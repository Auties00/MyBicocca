package it.attendance100.mybicocca.codegen.target.web

import it.attendance100.mybicocca.codegen.core.CodeWriter
import it.attendance100.mybicocca.codegen.core.Text

/** TypeScript lexical rules: reserved words, literals and comments. */
internal object TsSyntax {
    /** Words that can't name a variable or parameter (they can still name properties). */
    private val RESERVED = setOf(
        "break", "case", "catch", "class", "const", "continue", "debugger", "default", "delete",
        "do", "else", "enum", "export", "extends", "false", "finally", "for", "function", "if",
        "import", "in", "instanceof", "new", "null", "return", "super", "switch", "this", "throw",
        "true", "try", "typeof", "var", "void", "while", "with", "implements", "interface", "let",
        "package", "private", "protected", "public", "static", "yield", "await", "arguments", "eval",
    )

    private val IDENTIFIER = Regex("^[A-Za-z_$][A-Za-z0-9_$]*$")

    /** [name] as a variable or parameter name. */
    fun variable(name: String): String = when {
        name.first().isDigit() -> "_$name"
        name in RESERVED -> "${name}_"
        else -> name
    }

    /** [name] as a property key in an object literal or type, quoted when needed. */
    fun propertyKey(name: String): String = if (IDENTIFIER.matches(name)) name else string(name)

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

    /** The contents of a template literal, with backticks, backslashes and `${` escaped. */
    fun templateContent(value: String): String =
        value.replace("\\", "\\\\").replace("`", "\\`").replace("\${", "\\\${")

    fun docText(text: String): String = Text.singleLine(text).replace("*/", "*&#47;")

    fun CodeWriter.doc(text: String?) {
        if (text != null) line("/** ${docText(text)} */")
    }
}
