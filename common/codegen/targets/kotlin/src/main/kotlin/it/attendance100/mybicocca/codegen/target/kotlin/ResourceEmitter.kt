package it.attendance100.mybicocca.codegen.target.kotlin

import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.Resource

/**
 * The API's resources: `<Prefix>Resources`, whose functions decode each resource out of the JAR's
 * resources, where [path] says the target writes them, under the API's package path. They are
 * looked up by absolute path, which holds even when a shrinker moves the class to another package.
 */
internal class ResourceEmitter(private val spec: ApiSpec, private val layout: KotlinLayout) {
    val name = "${spec.prefix}Resources"

    /** Where [resource] lands in the resources, relative to their root. */
    fun path(resource: Resource): String = "${layout.apiPackage.replace('.', '/')}/${spec.id}/${resource.name}.json"

    fun emit(file: KotlinFile) {
        file.import("kotlinx.serialization.DeserializationStrategy")
        file.body.apply {
            line("/** The data the SDK ships along with its code, read out of its resources. */")
            block("object $name {") {
                for (resource in spec.resources) {
                    val description = resource.description?.let { "${KotlinSyntax.docText(it)} " }.orEmpty()
                    line("/** ${description}Decoded on every call: keep the result. */")
                    val type = layout.render(resource.type, file)
                    line("fun ${KotlinSyntax.identifier(resource.name)}(): $type = read(${KotlinSyntax.string("/" + path(resource))}, $type.serializer())")
                    line()
                }
                block("private fun <T> read(name: String, deserializer: DeserializationStrategy<T>): T {") {
                    line("val stream = $name::class.java.getResourceAsStream(name) ?: error(\"The SDK lacks its resource \$name\")")
                    line("return stream.use { ${layout.runtime.substringAfterLast('.')}.json.decodeFromString(deserializer, it.readBytes().decodeToString()) }")
                }
            }
        }
    }
}
