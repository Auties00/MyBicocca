package it.attendance100.mybicocca.codegen.esse3

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

class Dictionary(private val translations: LinkedHashMap<String, String>) {
    val keys: Set<String> get() = translations.keys

    fun translate(key: String): String {
        val value = translations[key]
        return if (value.isNullOrEmpty()) key else value
    }

    fun existingValueOrEmpty(key: String): String = translations[key] ?: ""

    companion object {
        fun load(file: File): Dictionary {
            if (!file.exists()) return Dictionary(linkedMapOf())
            val json = Json.parseToJsonElement(file.readText()).jsonObject
            val map = linkedMapOf<String, String>()
            for ((key, value) in json) {
                map[key] = value.jsonPrimitive.content
            }
            return Dictionary(map)
        }
    }
}
