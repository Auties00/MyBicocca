package it.attendance100.mybicocca.codegen.core

import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.Effect
import kotlinx.serialization.Serializable
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

/**
 * The effects of an API's operations kept next to its specs, in `effects.json`, for specs that
 * can't say themselves, such as the Esse3 and Moodle dumps: what each operation does to the
 * server's state (see [Effect]), keyed by `Module.operation`, with the reason for the choice.
 *
 * ```json
 * { "Careers.putCanteenBand": { "effect": "irreversible", "reason": "Changes the student's fee band." } }
 * ```
 *
 * An entry overrides the effect the reader found.
 */
object EffectsOverlay {
    const val FILE_NAME = "effects.json"

    /**
     * @property notes Risks a reviewer should know about.
     */
    @Serializable
    data class Entry(val effect: String, val reason: String, val notes: String? = null)

    /** [spec] with the effects of the overlay in [specDir], if there is one. */
    fun apply(spec: ApiSpec, specDir: Path): ApiSpec {
        val file = specDir.resolve(FILE_NAME)
        if (!file.exists()) return spec
        val entries = OptionsJson.decodeFromString<Map<String, Entry>>(file.readText())
        val operations = spec.modules.flatMap { module -> module.operations.map { "${module.name}.${it.name}" } }.toSet()
        val unknown = entries.keys - operations
        require(unknown.isEmpty()) { "$file names operations that don't exist: ${unknown.sorted()}" }
        val effects = entries.mapValues { (key, entry) ->
            Effect.fromWireName(entry.effect) ?: error("$file gives '$key' the unknown effect '${entry.effect}'")
        }
        return spec.copy(modules = spec.modules.map { module ->
            module.copy(operations = module.operations.map { operation ->
                effects["${module.name}.${operation.name}"]?.let { operation.copy(effect = it) } ?: operation
            })
        })
    }
}
