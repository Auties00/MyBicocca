package it.attendance100.mybicocca.codegen.target.swift

import it.attendance100.mybicocca.codegen.core.CodeWriter
import it.attendance100.mybicocca.codegen.core.GENERATED_MARKER
import it.attendance100.mybicocca.codegen.core.GeneratedFile

/** A Swift source file being built; every generated file imports Foundation and the runtime. */
internal class SwiftFile(private val fileName: String, private val runtimeModule: String) {
    val body = CodeWriter()

    fun toGeneratedFile(): GeneratedFile = GeneratedFile(
        fileName,
        "// $GENERATED_MARKER. Do not edit.\n\nimport $runtimeModule\nimport Foundation\n\n$body",
    )
}
