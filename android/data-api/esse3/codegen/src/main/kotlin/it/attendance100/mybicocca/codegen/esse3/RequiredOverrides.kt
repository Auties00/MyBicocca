package it.attendance100.mybicocca.codegen.esse3

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

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
