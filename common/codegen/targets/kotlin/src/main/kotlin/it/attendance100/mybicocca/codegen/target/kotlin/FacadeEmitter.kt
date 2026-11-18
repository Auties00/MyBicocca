package it.attendance100.mybicocca.codegen.target.kotlin

import it.attendance100.mybicocca.codegen.core.decapitalized
import it.attendance100.mybicocca.codegen.model.ApiSpec

/**
 * Emits the facade: the API's entry point, owning the HTTP client and exposing one property
 * per module, and one per client setting, which the caller may change at any time. The client
 * and JSON come from the API's generated `Runtime` object.
 */
internal class FacadeEmitter(private val spec: ApiSpec, private val layout: KotlinLayout) {

    fun emit(file: KotlinFile) {
        file.import("io.ktor.client.HttpClientConfig")
        file.import(layout.runtime)
        val runtime = layout.runtime.substringAfterLast('.')
        val parameters = spec.parameters.map { "${KotlinSyntax.identifier(it.name)}: ${layout.render(it.type, file)}" } +
            "httpClientConfig: HttpClientConfig<*>.() -> Unit = {}"
        file.body.apply {
            line("/**")
            line(" * Entry point of the ${spec.prefix} API: one property per module, all sharing one HTTP client.")
            line(" *")
            for (parameter in spec.parameters) {
                parameter.description?.let { line(" * @param ${KotlinSyntax.identifier(parameter.name)} ${KotlinSyntax.docText(it)}") }
            }
            line(" * @param httpClientConfig Additional configuration of the HTTP client, such as authentication.")
            line(" */")
            if (spec.parameters.isEmpty()) {
                line("class ${layout.facadeName}(${parameters.single()}) : AutoCloseable {")
            } else {
                commaList("class ${layout.facadeName}(", parameters, ") : AutoCloseable {")
            }
            indent {
                line("private val json = $runtime.json")
                line("private val settings = $runtime.settings()")
                line("private val client = $runtime.createHttpClient(json, settings, httpClientConfig)")
                line()
                for (setting in spec.client.settings) {
                    val description = setting.description?.let { "${KotlinSyntax.docText(it)} " }.orEmpty()
                    line("/** ${description}Defaults to `${KotlinSyntax.docText(setting.default)}`. */")
                    line("var ${KotlinSyntax.identifier(setting.name)}: String by settings")
                    line()
                }
                for (module in spec.modules.filter { it.operations.isNotEmpty() }) {
                    val arguments = listOf("client", "json") + layout.moduleParameters(module).map { KotlinSyntax.identifier(it.name) }
                    line("val ${KotlinSyntax.identifier(module.name.decapitalized())} = ${layout.serviceName(module)}(${arguments.joinToString(", ")})")
                }
                line()
                block("override fun close() {") {
                    line("client.close()")
                }
            }
            line("}")
        }
    }
}
