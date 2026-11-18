package it.attendance100.mybicocca.codegen.target.kotlin

import it.attendance100.mybicocca.codegen.model.ApiSpec

/** Emits the API's base URLs as an enum the runtime's request helpers take. */
internal class ServerEmitter(private val spec: ApiSpec, private val layout: KotlinLayout) {

    fun emit(file: KotlinFile) {
        file.body.apply {
            line("/** Base URLs of the ${spec.prefix} API. */")
            block("enum class ${layout.server.substringAfterLast('.')}(val url: String) {") {
                spec.servers.forEachIndexed { index, server ->
                    line("${server.name}(${KotlinSyntax.string(server.url)})" + if (index < spec.servers.lastIndex) "," else ";")
                }
            }
        }
    }
}
