package it.attendance100.mybicocca.codegen.target.kotlin

import it.attendance100.mybicocca.codegen.core.CodeWriter
import it.attendance100.mybicocca.codegen.core.generatedMarker
import it.attendance100.mybicocca.codegen.core.GeneratedFile

/**
 * A Kotlin source file being built: emitters write declarations into [body] and register the
 * classes they reference with [import], so the import list always matches what is used.
 */
internal class KotlinFile(private val packageName: String, private val fileName: String, private val apiId: String) {
    private val imports = sortedSetOf<String>()
    val body = CodeWriter()

    /** Imports [qualifiedName] unless it is in this file's package. */
    fun import(qualifiedName: String) {
        if (qualifiedName.substringBeforeLast('.') != packageName) imports += qualifiedName
    }

    fun toGeneratedFile(): GeneratedFile {
        val content = buildString {
            append("// ${generatedMarker(apiId)}. Do not edit.\n")
            append("package $packageName\n\n")
            if (imports.isNotEmpty()) {
                imports.forEach { append("import $it\n") }
                append('\n')
            }
            append(body)
        }
        return GeneratedFile("${packageName.replace('.', '/')}/$fileName", content)
    }
}
