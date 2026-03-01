package it.attendance100.mybicocca.codegen.esse3

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

/**
 * Overrides for required fields in OpenAPI specs.
 *
 * Entries are keyed as `DefinitionName.wireName`. When set to `false`, the field
 * is forced to be optional (nullable with default null) even if the spec marks it
 * as required. This is useful when the server returns `null` for fields that the
 * spec incorrectly declares as required.
 */
class RequiredOverrides(private val overrides: LinkedHashMap<String, Boolean>) {
    val keys: Set<String> get() = overrides.keys

    fun isRequired(definitionName: String, wireName: String): Boolean? {
        return overrides["$definitionName.$wireName"]
    }

    fun existingValueOrNull(key: String): Boolean? = overrides[key]

    companion object {
        fun load(file: File): RequiredOverrides {
            if (!file.exists()) return RequiredOverrides(linkedMapOf())
            val json = Json.parseToJsonElement(file.readText()).jsonObject
            val map = linkedMapOf<String, Boolean>()
            for ((key, value) in json) {
                map[key] = value.jsonPrimitive.boolean
            }
            return RequiredOverrides(map)
        }
    }
}
