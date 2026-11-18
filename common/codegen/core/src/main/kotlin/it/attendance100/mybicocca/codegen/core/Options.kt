package it.attendance100.mybicocca.codegen.core

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

/**
 * Decoder for plugin options. Unknown keys are rejected, so a misspelled option fails the
 * run instead of being silently ignored.
 */
val OptionsJson: Json = Json { ignoreUnknownKeys = false }

/** Decodes a plugin's `options` object into its typed options class. */
inline fun <reified T> JsonObject.decodeOptions(): T = OptionsJson.decodeFromJsonElement<T>(this)
