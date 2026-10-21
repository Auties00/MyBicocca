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
        val scrapers = SwiftScrapeEmitter(spec, layout, records)
        val services = SwiftServiceEmitter(spec, layout, scrapers)

        fun file(name: String, emit: (CodeWriter) -> Unit) =
            SwiftFile(name, layout.runtimeModule, spec.id).also { emit(it.body) }.toGeneratedFile()

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
            if (spec.authSchemes.isNotEmpty()) {
                add(file(layout.authFileName) { authTypes(spec, layout, it) })
            }
            if (spec.servers.isNotEmpty()) {
                add(file("${layout.server}.swift") { servers(spec, layout, it) })
            }
            if (scrapers.isNeeded) {
                add(file(scrapers.fileName) { scrapers.emit(it) })
            }
            add(file("${layout.facadeName}.swift") { facade(spec, layout, it) })
        }
    }

    /**
     * A protocol every scheme's credentials conform to, so operations accepting several
     * schemes take one set of credentials, and a struct per scheme building its headers.
     */
    private fun authTypes(spec: ApiSpec, layout: SwiftLayout, out: CodeWriter) {
        out.line("/// Credentials accepted by ${spec.prefix} operations.")
        out.line("public protocol ${layout.authBase}: RequestAuth {}")
        for (scheme in spec.authSchemes) {
            out.line()
            scheme.description?.let { out.line("/// ${SwiftSyntax.docText(it)}") }
            out.block("public struct ${layout.authClass(scheme.name)}: ${layout.authBase} {") {
                for (parameter in scheme.parameters) line("public var ${SwiftSyntax.identifier(parameter.name)}: String")
                line()
                val parameters = scheme.parameters.map { "${SwiftSyntax.identifier(it.name)}: String" }
                commaListOrLine("public init(", parameters, ") {", MAX_LINE_LENGTH)
                indent {
                    for (parameter in scheme.parameters) line("self.${SwiftSyntax.identifier(parameter.name)} = ${SwiftSyntax.identifier(parameter.name)}")
                }
                line("}")
                line()
                block("public var headers: [String: String] {") {
                    if (scheme.headers.isEmpty()) line("[:]")
                    else commaList("[", scheme.headers.map { (name, template) -> "${SwiftSyntax.string(name)}: ${headerTemplate(template)}" }, "]")
                }
                if (scheme.form.isNotEmpty()) {
                    line()
                    block("public var form: [String: String] {") {
                        commaList("[", scheme.form.map { (name, template) -> "${SwiftSyntax.string(name)}: ${headerTemplate(template)}" }, "]")
                    }
                }
            }
        }
    }

    /** A header value template (`Bearer {token}`) as a Swift string with interpolations. */
    private fun headerTemplate(template: String): String =
        PLACEHOLDER.replace(SwiftSyntax.string(template)) { "\\(${SwiftSyntax.identifier(it.groupValues[1])})" }

    private fun servers(spec: ApiSpec, layout: SwiftLayout, out: CodeWriter) {
        out.line("/// Base URLs of the ${spec.prefix} API.")
        out.block("public enum ${layout.server}: String, Sendable, CaseIterable {") {
            for (server in spec.servers) line("case ${layout.serverCase(server.name)} = ${SwiftSyntax.string(server.url)}")
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
            val documented = spec.parameters.filter { it.description != null }
            if (documented.isNotEmpty()) {
                line("/// - Parameters:")
                for (parameter in documented) line("///   - ${parameter.name}: ${SwiftSyntax.docText(parameter.description!!)}")
            }
            val parameters = listOf("client: ${layout.client}") + spec.parameters.map { "${SwiftSyntax.identifier(it.name)}: ${layout.render(it.type)}" }
            commaListOrLine("public init(", parameters, ") {", MAX_LINE_LENGTH)
            indent {
                for (module in modules) {
                    val arguments = listOf("client: client") + spec.parametersOf(module).map { "${it.name}: ${SwiftSyntax.identifier(it.name)}" }
                    line("self.${SwiftSyntax.identifier(module.name.decapitalized())} = ${layout.serviceName(module)}(${arguments.joinToString(", ")})")
                }
            }
            line("}")
        }
    }

    private companion object {
        const val MAX_LINE_LENGTH = 100
        val PLACEHOLDER = Regex("""\{([^}]+)}""")
    }
}
