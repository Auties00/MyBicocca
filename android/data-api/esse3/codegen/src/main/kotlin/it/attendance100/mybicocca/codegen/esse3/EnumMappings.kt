package it.attendance100.mybicocca.codegen.esse3

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

class EnumMappings(private val mappings: LinkedHashMap<String, LinkedHashMap<String, String>>) {
    val keys: Set<String> get() = mappings.keys

    fun isEnumField(definitionName: String, wireName: String): Boolean {
        return mappings.containsKey("$definitionName.$wireName")
    }

    fun getValueMappings(definitionName: String, wireName: String): Map<String, String>? {
        return mappings["$definitionName.$wireName"]
    }

    fun existingMappingOrNull(key: String): LinkedHashMap<String, String>? = mappings[key]

    companion object {
        fun load(file: File): EnumMappings {
            if (!file.exists()) return EnumMappings(linkedMapOf())
            val json = Json.parseToJsonElement(file.readText()).jsonObject
            val map = linkedMapOf<String, LinkedHashMap<String, String>>()
            for ((key, value) in json) {
                val inner = linkedMapOf<String, String>()
                for ((enumValue, kotlinName) in value.jsonObject) {
                    inner[enumValue] = kotlinName.jsonPrimitive.content
                }
                map[key] = inner
            }
            return EnumMappings(map)
        }
    }
}