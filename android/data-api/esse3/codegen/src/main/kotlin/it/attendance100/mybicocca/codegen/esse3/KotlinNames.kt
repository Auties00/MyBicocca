package it.attendance100.mybicocca.codegen.esse3

import it.attendance100.mybicocca.codegen.esse3.spec.ResolvedType

val KOTLIN_KEYWORDS = setOf(
    "as", "break", "class", "continue", "do", "else", "false", "for",
    "fun", "if", "in", "interface", "is", "null", "object", "package",
    "return", "super", "this", "throw", "true", "try", "typealias",
    "typeof", "val", "var", "when", "while"
)

fun escapeKotlinKeyword(name: String): String {
    return if (name in KOTLIN_KEYWORDS) "`$name`" else name
}

private val PARAM_SPLIT_REGEX = Regex("[^a-zA-Z0-9_]")

fun sanitizeParamName(name: String): String {
    val sanitized = name.split(PARAM_SPLIT_REGEX)
        .filter { it.isNotEmpty() }
        .mapIndexed { index, part ->
            if (index == 0) part.replaceFirstChar { it.lowercaseChar() }
            else part.replaceFirstChar { it.uppercaseChar() }
        }
        .joinToString("")
    return escapeKotlinKeyword(sanitized.ifEmpty { "param" })
}

fun ResolvedType.renderTranslated(glossary: Glossary, prefix: String = "Esse3"): String = when (this) {
    is ResolvedType.Simple -> kotlinType
    is ResolvedType.Reference -> {
        val fullName = "$prefix$definitionName"
        glossary.translate(fullName)
    }
    is ResolvedType.ListOf -> "List<${inner.renderTranslated(glossary, prefix)}>"
}
