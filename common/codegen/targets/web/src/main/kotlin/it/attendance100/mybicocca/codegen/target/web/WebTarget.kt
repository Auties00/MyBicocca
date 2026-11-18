package it.attendance100.mybicocca.codegen.target.web

import it.attendance100.mybicocca.codegen.core.CodeTarget
import it.attendance100.mybicocca.codegen.core.GeneratedFile
import it.attendance100.mybicocca.codegen.core.TargetContext
import it.attendance100.mybicocca.codegen.core.decapitalized
import it.attendance100.mybicocca.codegen.core.SHARED_FILES_OWNER
import it.attendance100.mybicocca.codegen.core.decodeOptions
import it.attendance100.mybicocca.codegen.core.resourceFiles
import it.attendance100.mybicocca.codegen.model.ApiSpec
import kotlinx.serialization.Serializable

/**
 * Generates a TypeScript SDK for the web app, built on `fetch` and usable in browsers and Node:
 * the API's modules in the folder of its subpackage, the runtime they build on as its shared
 * files, and an `index.ts` re-exporting each folder. See [TsLayout] for the names and files it
 * uses.
 */
class WebTarget : CodeTarget {
    override val id: String = "web"

    /**
     * @property packageName The subpackage, and folder, the API's modules go in; by default the
     *   API's id. APIs of the same service share one (e.g. `elearning`).
     * @property runtimePath The folder of the runtime.
     */
    @Serializable
    data class Options(
        val packageName: String? = null,
        val runtimePath: String = "runtime",
    )

    override fun generate(spec: ApiSpec, context: TargetContext): List<GeneratedFile> {
        val options = context.options.decodeOptions<Options>()
        val layout = TsLayout(spec, options.packageName ?: spec.id, options.runtimePath)
        val codecs = TsCodecs(layout)
        val records = TsRecordEmitter(layout, codecs)
        val enums = TsEnumEmitter(layout)
        val scrapers = TsScrapeEmitter(spec, layout)
        val services = TsServiceEmitter(spec, layout, codecs, scrapers)

        fun file(name: String, emit: (TsFile) -> Unit) = TsFile(name, spec.id).also(emit).toGeneratedFile()

        return buildList {
            for (module in spec.modules) {
                if (module.records.isNotEmpty()) {
                    add(file(layout.recordsFile(module)) { file -> file.body.separated(module.records) { records.emit(it, file) } })
                }
                if (module.operations.isNotEmpty()) {
                    add(file(layout.serviceFile(module)) { services.emit(module, it) })
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
            if (scrapers.isNeeded) {
                add(file(scrapers.fileName) { scrapers.emit(it) })
            }
            val client = TsClientEmitter(spec, layout)
            add(file(layout.facadeFile) { facade(spec, layout, it) })
            add(file(layout.clientFile) { client.emitClient(it) })
            add(file(layout.errorFile) { client.emitError(it) })
            if (spec.usesRoles) {
                add(file(layout.rolesFile) { client.emitRoles(it) })
            }
        }
    }

    /** The runtime, from this module's `web-runtime` resources. */
    override fun sharedFiles(context: TargetContext): List<GeneratedFile> =
        resourceFiles(javaClass, "web-runtime", RUNTIME_FILES, context.options.decodeOptions<Options>().runtimePath)

    /**
     * An `index.ts` in every folder holding the APIs' modules, but the output directory itself,
     * re-exporting its modules and its folders' indexes: `elearning/index.ts` re-exports
     * `elearning/api/index.ts`, which re-exports the API modules of every e-learning API.
     */
    override fun indexFiles(paths: Set<String>, context: TargetContext): List<GeneratedFile> {
        val modules = paths.filter { it.endsWith(".ts") }
        val folders = modules.flatMap { path -> path.split('/').dropLast(1).runningReduce { parent, name -> "$parent/$name" } }.toSortedSet()
        return folders.map { folder ->
            val index = TsFile("$folder/index.ts", SHARED_FILES_OWNER)
            val exported = modules.filter { it.substringBeforeLast('/') == folder } +
                folders.filter { it.substringBeforeLast('/', "") == folder }.map { "$it/index.ts" }
            exported.sorted().forEach { index.body.line("export * from ${TsSyntax.string(TsFile.relativeModule(index.path, it))};") }
            index.toGeneratedFile()
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
                    if (scheme.form.isNotEmpty()) {
                        line()
                        block("get form(): Readonly<Record<string, string>> {") {
                            block("return {", "};") {
                                for ((name, template) in scheme.form) line("${TsSyntax.string(name)}: ${headerTemplate(template)},")
                            }
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
        file.importFile(layout.client, layout.clientFile)
        modules.forEach { file.importFile(layout.serviceName(it), layout.serviceFile(it)) }
        if (spec.parameters.isNotEmpty()) file.importFile(layout.parameters, layout.parametersFile)
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
        val RUNTIME_FILES = listOf("client.ts", "coding.ts", "index.ts", "rest.ts", "scrape.ts", "temporal.ts")
        val PLACEHOLDER = Regex("""\{([^}]+)}""")
    }
}
