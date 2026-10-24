package it.attendance100.mybicocca.codegen.target.web

import it.attendance100.mybicocca.codegen.core.CodeWriter
import it.attendance100.mybicocca.codegen.core.GENERATED_MARKER
import it.attendance100.mybicocca.codegen.core.GeneratedFile

/**
 * A TypeScript module being built: emitters write into [body] and register what they use
 * with [import], so that every import is used (as `noUnusedLocals` requires).
 */
internal class TsFile(private val fileName: String) {
    /** Packages first, then relative modules, each alphabetically. */
    private val imports = sortedMapOf<String, MutableSet<String>>(compareBy({ it.startsWith(".") }, { it }))
    val body = CodeWriter("  ")

    /** Imports [name] from the module [from], unless [from] is this file. */
    fun import(name: String, from: String) {
        if (from == "./${fileName.removeSuffix(".ts")}.js") return
        imports.getOrPut(from) { sortedSetOf() } += name
    }

    fun toGeneratedFile(): GeneratedFile {
        val content = buildString {
            append("// $GENERATED_MARKER. Do not edit.\n\n")
            for ((module, names) in imports) {
                append("import { ${names.joinToString(", ")} } from ${TsSyntax.string(module)};\n")
            }
            if (imports.isNotEmpty()) append('\n')
            append(body)
        }
        return GeneratedFile(fileName, content)
    }
}
