package it.attendance100.mybicocca.codegen.target.web

import it.attendance100.mybicocca.codegen.core.CodeTarget
import it.attendance100.mybicocca.codegen.core.GeneratedFile
import it.attendance100.mybicocca.codegen.core.TargetContext
import it.attendance100.mybicocca.codegen.core.decapitalized
import it.attendance100.mybicocca.codegen.core.decodeOptions
import it.attendance100.mybicocca.codegen.model.ApiSpec
import kotlinx.serialization.Serializable

/**
 * Generates a TypeScript SDK for the web app, built on `fetch` and usable in browsers and
 * Node. See [TsLayout] for the names it uses and the runtime the API provides by hand.
 */
class WebTarget : CodeTarget {
    override val id: String = "web"

    /**
     * @property runtimeModule Import specifier of the shared hand-written runtime package.
     * @property clientModule Import specifier of the API's hand-written client, relative to the
     *   output directory.
     */
    @Serializable
    data class Options(
        val runtimeModule: String = "@mybicocca/data-api-runtime",
        val clientModule: String = "../client.js",
    )

    override fun generate(spec: ApiSpec, context: TargetContext): List<GeneratedFile> {
        val options = context.options.decodeOptions<Options>()
        val layout = TsLayout(spec, options.runtimeModule, options.clientModule)
        val codecs = TsCodecs(layout)
        val records = TsRecordEmitter(layout, codecs)
        val enums = TsEnumEmitter(layout)
        val services = TsServiceEmitter(spec, layout, codecs)

        fun file(name: String, emit: (TsFile) -> Unit) = TsFile(name).also(emit).toGeneratedFile()

        val files = buildList {
            for (module in spec.modules) {
                if (module.records.isNotEmpty()) {
                    add(file(layout.recordsFile(module)) { file -> file.body.separated(module.records) { records.emit(it, file) } })
                }
                if (module.operations.isNotEmpty()) {
                    add(file("${layout.serviceName(module)}.ts") { services.emit(module, it) })
                }
            }
            if (spec.enums.isNotEmpty()) {
                add(file(layout.enumsFile) { file -> file.body.separated(spec.enums) { enums.emit(it, file) } })
            }
            if (spec.authSchemes.isNotEmpty()) {
                add(file(layout.authFile) { authTypes(spec, layout, it) })
            }
            if (spec.servers.isNotEmpty()) {
                add(file(layout.serverFile) { servers(spec, layout, it) })
            }
            if (spec.parameters.isNotEmpty()) {
                add(file(layout.parametersFile) { parameters(spec, layout, it) })
            }
            add(file("${layout.facadeName}.ts") { facade(spec, layout, it) })
        }
        return files + file("index.ts") { index ->
            files.forEach { index.body.line("export * from ${TsSyntax.string(layout.moduleOf(it.path))};") }
        }
    }

    /** A class per scheme building its headers, and the union of all of them. */
    private fun authTypes(spec: ApiSpec, layout: TsLayout, file: TsFile) {
        layout.importRuntime(file, "RequestAuth")
        file.body.apply {
            for (scheme in spec.authSchemes) {
                scheme.description?.let { line("/** ${TsSyntax.docText(it)} */") }
                block("export class ${layout.authClass(scheme.name)} implements RequestAuth {") {
                    val parameters = scheme.parameters.map { "readonly ${TsSyntax.variable(it.name)}: string" }
                    commaListOrLine("constructor(", parameters, ") {}", MAX_LINE_LENGTH, trailingComma = true)
                    line()
                    block("get headers(): Readonly<Record<string, string>> {") {
                        block("return {", "};") {
                            for ((name, template) in scheme.headers) line("${TsSyntax.string(name)}: ${headerTemplate(template)},")
                        }
                    }
                }
                line()
            }
            line("/** Credentials accepted by ${spec.prefix} operations. */")
            line("export type ${layout.authBase} = ${spec.authSchemes.joinToString(" | ") { layout.authClass(it.name) }};")
        }
    }

    /** A header value template (`Bearer {token}`) as a template literal over `this`. */
    private fun headerTemplate(template: String): String {
        val literals = PLACEHOLDER.split(template).map(TsSyntax::templateContent)
        val placeholders = PLACEHOLDER.findAll(template).map { "\${this.${TsSyntax.variable(it.groupValues[1])}}" }.toList()
        return "`" + literals.zip(placeholders + "").joinToString("") { (literal, placeholder) -> literal + placeholder } + "`"
    }

    private fun servers(spec: ApiSpec, layout: TsLayout, file: TsFile) {
        file.body.apply {
            line("/** Base URLs of the ${spec.prefix} API. */")
            block("export const ${layout.server} = {", "} as const;") {
                for (server in spec.servers) line("${TsSyntax.propertyKey(server.name)}: ${TsSyntax.string(server.url)},")
            }
            line()
            line("export type ${layout.server} = (typeof ${layout.server})[keyof typeof ${layout.server}];")
        }
    }

    private fun parameters(spec: ApiSpec, layout: TsLayout, file: TsFile) {
        file.body.apply {
            line("/** Values every ${spec.prefix} operation uses, given once when creating the API. */")
            block("export interface ${layout.parameters} {") {
                for (parameter in spec.parameters) {
                    parameter.description?.let { line("/** ${TsSyntax.docText(it)} */") }
                    line("${TsSyntax.propertyKey(parameter.name)}: ${layout.render(parameter.type, file)};")
                }
            }
        }
    }

    private fun facade(spec: ApiSpec, layout: TsLayout, file: TsFile) {
        val modules = spec.modules.filter { it.operations.isNotEmpty() }
        file.import(layout.client, layout.clientModule)
        modules.forEach { file.import(layout.serviceName(it), layout.moduleOf("${layout.serviceName(it)}.ts")) }
        if (spec.parameters.isNotEmpty()) file.import(layout.parameters, layout.moduleOf(layout.parametersFile))
        file.body.apply {
            line("/** Entry point of the ${spec.prefix} API: one property per module, all sharing one client. */")
            block("export class ${layout.facadeName} {") {
                for (module in modules) {
                    line("readonly ${TsSyntax.propertyKey(module.name.decapitalized())}: ${layout.serviceName(module)};")
                }
                line()
                val (parameters, arguments) = if (spec.parameters.isEmpty()) {
                    "readonly client: ${layout.client}" to "client"
                } else {
                    "readonly client: ${layout.client}, readonly parameters: ${layout.parameters}" to "client, parameters"
                }
                block("constructor($parameters) {") {
                    for (module in modules) {
                        line("this.${module.name.decapitalized()} = new ${layout.serviceName(module)}($arguments);")
                    }
                }
            }
        }
    }

    private companion object {
        const val MAX_LINE_LENGTH = 100
        val PLACEHOLDER = Regex("""\{([^}]+)}""")
    }
}
