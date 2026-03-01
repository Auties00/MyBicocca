package it.attendance100.mybicocca.codegen.esse3

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

class EnumValueFields(private val fields: LinkedHashMap<String, Boolean>) {
    val keys: Set<String> get() = fields.keys

    fun needsSerializer(definitionName: String, wireName: String): Boolean {
        return fields["$definitionName.$wireName"] == true
    }

    fun existingValueOrNull(key: String): Boolean? = fields[key]

    companion object {
        fun load(file: File): EnumValueFields {
            if (!file.exists()) return EnumValueFields(linkedMapOf())
            val json = Json.parseToJsonElement(file.readText()).jsonObject
            val map = linkedMapOf<String, Boolean>()
            for ((key, value) in json) {
                map[key] = value.jsonPrimitive.boolean
            }
            return EnumValueFields(map)
        }
    }
}
