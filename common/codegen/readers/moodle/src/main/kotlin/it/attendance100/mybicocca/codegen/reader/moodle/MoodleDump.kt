package it.attendance100.mybicocca.codegen.reader.moodle

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.nio.file.Path
import kotlin.io.path.readText

/**
 * The web service functions dumped from a Moodle source tree by
 * `common/specs/moodle/tools/dump-functions.php`.
 *
 * @property moodle The Moodle release the functions were dumped from.
 */
@Serializable
internal data class MoodleDump(val moodle: String, val functions: List<DumpFunction>) {
    companion object {
        private val json = Json {
            classDiscriminator = "kind"
            ignoreUnknownKeys = true
        }

        fun load(file: Path): MoodleDump = json.decodeFromString(serializer(), file.readText())
    }
}

/**
 * A web service function.
 *
 * @property component The Moodle component declaring it (e.g. `core_course`, `mod_forum`).
 * @property loginrequired Whether a call needs a user's token.
 * @property parameters An object description whose keys are the function's parameters.
 * @property returns What the function returns, or null when it returns nothing.
 */
@Serializable
internal data class DumpFunction(
    val name: String,
    val component: String,
    val type: String = "read",
    val loginrequired: Boolean = true,
    val description: String = "",
    val parameters: Description.Object,
    val returns: Description? = null,
)

/** How a Moodle `external_description` requires its value. */
@Serializable
internal enum class Presence {
    @SerialName("required") REQUIRED,
    @SerialName("optional") OPTIONAL,

    /** The value is optional and defaults to [Description.Value.default] or [Description.ListOf.default]. */
    @SerialName("default") DEFAULT,
}

/** A Moodle `external_description`: a value, an object or a list. */
@Serializable
internal sealed interface Description {
    val presence: Presence
    val nullable: Boolean
    val description: String

    /**
     * An `external_value`.
     *
     * @property type The Moodle `PARAM_*` type, without the prefix and in lowercase (`int`,
     *   `bool`, `raw`, …).
     */
    @Serializable
    @SerialName("value")
    data class Value(
        val type: String,
        override val presence: Presence,
        override val nullable: Boolean = false,
        override val description: String = "",
        val default: JsonElement? = null,
    ) : Description

    /** An `external_single_structure`, with its keys in declaration order. */
    @Serializable
    @SerialName("object")
    data class Object(
        val keys: Map<String, Description>,
        override val presence: Presence = Presence.REQUIRED,
        override val nullable: Boolean = false,
        override val description: String = "",
    ) : Description

    /** An `external_multiple_structure`. */
    @Serializable
    @SerialName("list")
    data class ListOf(
        val content: Description,
        override val presence: Presence,
        override val nullable: Boolean = false,
        override val description: String = "",
        val default: JsonElement? = null,
    ) : Description
}
