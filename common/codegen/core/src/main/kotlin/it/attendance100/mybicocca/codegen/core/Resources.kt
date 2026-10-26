package it.attendance100.mybicocca.codegen.core

import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.EnumType
import it.attendance100.mybicocca.codegen.model.PrimitiveKind
import it.attendance100.mybicocca.codegen.model.RecordType
import it.attendance100.mybicocca.codegen.model.Resource
import it.attendance100.mybicocca.codegen.model.TypeRef
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

/**
 * Fetches the data of a [Resource] again, typically by crawling a website.
 *
 * Implementations are discovered with [java.util.ServiceLoader]: a module lists its class in
 * `META-INF/services/it.attendance100.mybicocca.codegen.core.ResourceBuilder`.
 */
interface ResourceBuilder {
    /** Identifier the `builder` of a resource names. */
    val id: String

    /** The resource's data, as its type describes it. */
    fun build(context: ResourceContext): JsonObject
}

/**
 * @property previous The data the resource had until now, if any: a builder may keep what can't
 *   have changed, such as past academic years, rather than fetch it again.
 * @property checkpoint Saves the data built so far, so that a crawl stopped halfway resumes from
 *   it as its [previous] data; the data is checked only once complete.
 */
class ResourceContext(val previous: JsonObject?, val checkpoint: (JsonObject) -> Unit = {}, val log: (String) -> Unit)

/**
 * The data of an API's resources, kept in its spec directory as `resources/<name>.json`: the
 * input every target writes into its platform's resources, so that generating and checking
 * never need the network. [refresh] fetches it again with each resource's builder.
 */
object ResourceData {
    const val DIRECTORY = "resources"

    /** Each resource's data: fetched again and saved first when [refresh]. */
    fun load(specDir: Path, spec: ApiSpec, refresh: Boolean, registry: Plugins, log: (String) -> Unit): Map<Resource, JsonObject> =
        spec.resources.associateWith { resource ->
            val file = specDir.resolve(DIRECTORY).resolve("${resource.name}.json")
            val saved = if (file.exists()) OptionsJson.parseToJsonElement(file.readText()) as? JsonObject ?: error("$file must hold a JSON object") else null
            val data = if (refresh) {
                log("Building the resource '${resource.name}' of '${spec.id}' with ${resource.builder}")
                val checkpoint = { partial: JsonObject ->
                    file.parent.createDirectories()
                    file.writeText(partial.toString())
                }
                registry.resourceBuilder(resource.builder).build(ResourceContext(saved, checkpoint, log))
            } else {
                saved ?: error("No data for the resource '${resource.name}' of '${spec.id}' at $file: fetch it with --resources")
            }
            val problems = validate(spec, resource.type, data, resource.name)
            require(problems.isEmpty()) {
                "The data of the resource '${resource.name}' of '${spec.id}' doesn't fit ${resource.type.name}:\n" +
                    problems.take(20).joinToString("\n") { "  - $it" }
            }
            if (refresh) {
                file.parent.createDirectories()
                file.writeText(data.toString())
            }
            data
        }

    /** Where [data] doesn't fit [type]: missing required keys, unknown keys, values of another type. */
    fun validate(spec: ApiSpec, type: TypeRef, data: JsonElement, path: String): List<String> = buildList {
        fun problem(message: String) {
            add(message)
        }

        fun check(type: TypeRef, value: JsonElement, at: String) {
            when (type) {
                is TypeRef.Named -> when (val declaration = spec.resolve(type)) {
                    is RecordType -> {
                        val obj = value as? JsonObject ?: return problem("$at: expected an object")
                        for (field in declaration.fields) {
                            val item = obj[field.wireName]
                            when {
                                item == null -> if (field.default == null && !field.nullable) add("$at.${field.wireName}: missing")
                                item is JsonNull -> if (!field.nullable) add("$at.${field.wireName}: null")
                                else -> check(field.type, item, "$at.${field.wireName}")
                            }
                        }
                        val known = declaration.fields.map { it.wireName }.toSet()
                        for (key in obj.keys - known) add("$at.$key: not in ${declaration.name}")
                    }
                    is EnumType -> {
                        val text = (value as? JsonPrimitive)?.content ?: return problem("$at: expected a value of ${declaration.name}")
                        if (!declaration.open && declaration.values.none { it.wireValue == text }) add("$at: '$text' isn't a value of ${declaration.name}")
                    }
                }
                is TypeRef.ListOf -> (value as? JsonArray ?: return problem("$at: expected a list")).forEachIndexed { index, item -> check(type.element, item, "$at[$index]") }
                is TypeRef.MapOf -> (value as? JsonObject ?: return problem("$at: expected an object")).forEach { (key, item) -> check(type.value, item, "$at.$key") }
                is TypeRef.Primitive -> {
                    val primitive = value as? JsonPrimitive
                    val fits = when (type.kind) {
                        PrimitiveKind.JSON_OBJECT -> value is JsonObject
                        PrimitiveKind.JSON_VALUE -> true
                        PrimitiveKind.BOOLEAN -> primitive?.booleanOrNull != null && !primitive.isString
                        PrimitiveKind.INT32, PrimitiveKind.INT64 -> primitive?.longOrNull != null && !primitive.isString
                        PrimitiveKind.FLOAT32, PrimitiveKind.FLOAT64 -> primitive?.doubleOrNull != null && !primitive.isString
                        else -> primitive?.isString == true
                    }
                    if (!fits) add("$at: expected ${type.kind.name.lowercase()}, not $value")
                }
            }
        }
        check(type, data, path)
    }
}

/**
 * The file a target writes a resource's [data] into: minified JSON, the same on every platform,
 * whose first key carries the [generatedMarker] of [apiId] (decoders ignore it), since JSON has no
 * comments.
 */
fun resourceJson(apiId: String, data: JsonObject): String =
    buildJsonObject {
        put(GENERATED_KEY, JsonPrimitive("${generatedMarker(apiId)}. Do not edit."))
        for ((key, value) in data) put(key, value)
    }.toString()

/** The key of a resource file carrying its [generatedMarker]. */
const val GENERATED_KEY = "_generated"
