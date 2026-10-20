package it.attendance100.mybicocca.codegen.target.kotlin

import it.attendance100.mybicocca.codegen.core.decapitalized
import it.attendance100.mybicocca.codegen.model.ApiSpec

/**
 * Emits the facade: the API's entry point, owning the HTTP client and exposing one property
 * per module. How the client and JSON are configured is up to the API's hand-written
 * `Runtime` object.
 */
internal class FacadeEmitter(private val spec: ApiSpec, private val layout: KotlinLayout) {

    fun emit(file: KotlinFile) {
        file.import("io.ktor.client.HttpClientConfig")
        file.import(layout.runtime)
        val runtime = layout.runtime.substringAfterLast('.')
        file.body.apply {
            line("/**")
            line(" * Entry point of the ${spec.prefix} API: one property per module, all sharing one HTTP client.")
            line(" *")
            line(" * @param httpClientConfig Additional configuration of the HTTP client, such as authentication.")
            line(" */")
            block("class ${layout.facadeName}(httpClientConfig: HttpClientConfig<*>.() -> Unit = {}) : AutoCloseable {") {
                line("private val json = $runtime.json")
                line("private val client = $runtime.createHttpClient(json, httpClientConfig)")
                line()
                for (module in spec.modules.filter { it.operations.isNotEmpty() }) {
                    line("val ${KotlinSyntax.identifier(module.name.decapitalized())} = ${layout.serviceName(module)}(client, json)")
                }
                line()
                block("override fun close() {") {
                    line("client.close()")
                }
            }
        }
    }
}
