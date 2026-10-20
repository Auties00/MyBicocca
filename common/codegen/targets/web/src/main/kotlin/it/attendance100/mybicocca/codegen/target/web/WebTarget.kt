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
            add(file("${layout.facadeName}.ts") { facade(spec, layout, it) })
        }
        return files + file("index.ts") { index ->
            files.forEach { index.body.line("export * from ${TsSyntax.string(layout.moduleOf(it.path))};") }
        }
    }

    private fun facade(spec: ApiSpec, layout: TsLayout, file: TsFile) {
        val modules = spec.modules.filter { it.operations.isNotEmpty() }
        file.import(layout.client, layout.clientModule)
        modules.forEach { file.import(layout.serviceName(it), layout.moduleOf("${layout.serviceName(it)}.ts")) }
        file.body.apply {
            line("/** Entry point of the ${spec.prefix} API: one property per module, all sharing one client. */")
            block("export class ${layout.facadeName} {") {
                for (module in modules) {
                    line("readonly ${TsSyntax.propertyKey(module.name.decapitalized())}: ${layout.serviceName(module)};")
                }
                line()
                block("constructor(readonly client: ${layout.client}) {") {
                    for (module in modules) {
                        line("this.${module.name.decapitalized()} = new ${layout.serviceName(module)}(client);")
                    }
                }
            }
        }
    }
}
