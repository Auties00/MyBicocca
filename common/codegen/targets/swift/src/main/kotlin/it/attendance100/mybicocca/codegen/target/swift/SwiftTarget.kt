package it.attendance100.mybicocca.codegen.target.swift

import it.attendance100.mybicocca.codegen.core.CodeTarget
import it.attendance100.mybicocca.codegen.core.CodeWriter
import it.attendance100.mybicocca.codegen.core.GeneratedFile
import it.attendance100.mybicocca.codegen.core.TargetContext
import it.attendance100.mybicocca.codegen.core.decapitalized
import it.attendance100.mybicocca.codegen.core.decodeOptions
import it.attendance100.mybicocca.codegen.model.ApiSpec
import kotlinx.serialization.Serializable

/**
 * Generates a Swift SDK built on `URLSession` and `Codable`, as the sources of one Swift
 * module. See [SwiftLayout] for the names it uses and the runtime the API provides by hand.
 */
class SwiftTarget : CodeTarget {
    override val id: String = "swift"

    /** @property runtimeModule The Swift module of the shared hand-written runtime. */
    @Serializable
    data class Options(
        val runtimeModule: String = "DataApiRuntime",
    )

    override fun generate(spec: ApiSpec, context: TargetContext): List<GeneratedFile> {
        val options = context.options.decodeOptions<Options>()
        val layout = SwiftLayout(spec, options.runtimeModule)
        val records = SwiftRecordEmitter(layout)
        val enums = SwiftEnumEmitter(layout)
        val services = SwiftServiceEmitter(spec, layout)

        fun file(name: String, emit: (CodeWriter) -> Unit) =
            SwiftFile(name, layout.runtimeModule).also { emit(it.body) }.toGeneratedFile()

        return buildList {
            for (module in spec.modules) {
                if (module.records.isNotEmpty()) {
                    add(file(layout.recordsFileName(module)) { out -> out.separated(module.records) { records.emit(it, out) } })
                }
                if (module.operations.isNotEmpty()) {
                    add(file("${layout.serviceName(module)}.swift") { services.emit(module, it) })
                }
            }
            if (spec.enums.isNotEmpty()) {
                add(file(layout.enumsFileName) { out -> out.separated(spec.enums) { enums.emit(it, out) } })
            }
            add(file("${layout.facadeName}.swift") { facade(spec, layout, it) })
        }
    }

    private fun facade(spec: ApiSpec, layout: SwiftLayout, out: CodeWriter) {
        val modules = spec.modules.filter { it.operations.isNotEmpty() }
        out.line("/// Entry point of the ${spec.prefix} API: one property per module, all sharing one client.")
        out.block("public struct ${layout.facadeName}: Sendable {") {
            for (module in modules) {
                line("public let ${SwiftSyntax.identifier(module.name.decapitalized())}: ${layout.serviceName(module)}")
            }
            line()
            block("public init(client: ${layout.client}) {") {
                for (module in modules) {
                    line("self.${SwiftSyntax.identifier(module.name.decapitalized())} = ${layout.serviceName(module)}(client: client)")
                }
            }
        }
    }
}
