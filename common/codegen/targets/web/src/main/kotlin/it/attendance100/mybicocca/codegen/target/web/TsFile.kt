package it.attendance100.mybicocca.codegen.target.web

import it.attendance100.mybicocca.codegen.core.CodeWriter
import it.attendance100.mybicocca.codegen.core.generatedMarker
import it.attendance100.mybicocca.codegen.core.GeneratedFile

/**
 * A TypeScript module being built at [path], relative to the output directory: emitters write
 * into [body] and register what they use with [import] or [importFile], so that every import is
 * used (as `noUnusedLocals` requires).
 */
internal class TsFile(val path: String, private val apiId: String) {
    /** Packages first, then relative modules, each alphabetically. */
    private val imports = sortedMapOf<String, MutableSet<String>>(compareBy({ it.startsWith(".") }, { it }))
    val body = CodeWriter("  ")

    /** Imports [name] from the package [from]. */
    fun import(name: String, from: String) {
        imports.getOrPut(from) { sortedSetOf() } += name
    }

    /** Imports [name] from the generated file at [path], unless it is this file. */
    fun importFile(name: String, path: String) {
        if (path != this.path) import(name, relativeModule(this.path, path))
    }

    fun toGeneratedFile(): GeneratedFile {
        val content = buildString {
            append("// ${generatedMarker(apiId)}. Do not edit.\n\n")
            for ((module, names) in imports) {
                append("import { ${names.joinToString(", ")} } from ${TsSyntax.string(module)};\n")
            }
            if (imports.isNotEmpty()) append('\n')
            append(body)
        }
        return GeneratedFile(path, content)
    }

    companion object {
        /** The import specifier of the file at [to] in a file at [from], both relative to one directory. */
        fun relativeModule(from: String, to: String): String {
            val fromFolders = from.split('/').dropLast(1)
            val toParts = to.split('/')
            val common = fromFolders.zip(toParts).takeWhile { (a, b) -> a == b }.size
            val up = List(fromFolders.size - common) { ".." }
            val path = (up + toParts.drop(common)).joinToString("/").removeSuffix(".ts") + ".js"
            return if (up.isEmpty()) "./$path" else path
        }
    }
}
