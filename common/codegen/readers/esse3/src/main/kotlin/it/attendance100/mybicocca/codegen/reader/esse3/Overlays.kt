package it.attendance100.mybicocca.codegen.reader.esse3

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

/**
 * Translations of the original Esse3 names (Italian words and CINECA acronyms) into English
 * ones. Every name the reader meets is a key; an empty value means "not translated yet", in
 * which case the original name is kept.
 */
internal class Glossary(private val entries: Map<String, String>) {
    /** The keys, in file order. */
    val keys: Set<String> get() = entries.keys

    fun translate(name: String): String = entries[name]?.ifEmpty { null } ?: name

    operator fun get(name: String): String? = entries[name]

    companion object {
        fun load(file: Path): Glossary = Glossary(readJsonObject(file).mapValues { it.value.jsonPrimitive.content })
    }
}

/**
 * Names of enum values, by `Definition.property` or `operationId.parameter` and then by wire
 * value. An empty name falls back to the wire value in PascalCase.
 */
internal class EnumNames(private val entries: Map<String, Map<String, String>>) {
    operator fun get(key: String): Map<String, String>? = entries[key]

    companion object {
        fun load(file: Path): EnumNames =
            EnumNames(readJsonObject(file).mapValues { (_, values) -> values.jsonObject.mapValues { it.value.jsonPrimitive.content } })
    }
}

/**
 * Overrides of the `required` flags of the specs, by `Definition.property`. Esse3 marks
 * fields as required that its responses omit; setting one to `false` makes it optional.
 */
internal class RequiredOverrides(private val entries: Map<String, Boolean>) {
    /** The keys, in file order. */
    val keys: Set<String> get() = entries.keys

    operator fun get(key: String): Boolean? = entries[key]

    companion object {
        fun load(file: Path): RequiredOverrides = RequiredOverrides(readJsonObject(file).mapValues { it.value.jsonPrimitive.boolean })
    }
}

private val OverlayJson = Json { prettyPrint = true }

private fun readJsonObject(file: Path): JsonObject =
    if (file.exists()) Json.parseToJsonElement(file.readText()).jsonObject else JsonObject(emptyMap())

internal fun writeJsonObject(file: Path, content: Map<String, Any>) {
    file.writeText(OverlayJson.encodeToString(JsonObject.serializer(), content.toJsonObject()))
}

@Suppress("UNCHECKED_CAST")
private fun Map<String, Any>.toJsonObject(): JsonObject = JsonObject(
    mapValues { (_, value) ->
        when (value) {
            is String -> JsonPrimitive(value)
            is Boolean -> JsonPrimitive(value)
            is Map<*, *> -> (value as Map<String, Any>).toJsonObject()
            else -> error("Unsupported overlay value: $value")
        }
    }
)
