package it.attendance100.mybicocca.codegen.reader.moodle

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

/**
 * A hand-maintained overlay file: a JSON object of names by key, where an empty name means
 * "not chosen yet", in which case the reader derives one.
 *
 * The overlays of the Moodle API:
 * - `modules.json`: module names by Moodle component (`core_course` → `Course`);
 * - `types.json`: type names by the path of a structure in the dump
 *   (`core_course_get_contents/returns[]` → `CourseSection`), without the API's prefix;
 * - `names.json`: member names by wire name, since Moodle runs words together
 *   (`timemodified` → `timeModified`).
 */
internal class Overlay(private val entries: Map<String, String>) {
    /** The chosen name for [key], or null when there is none. */
    operator fun get(key: String): String? = entries[key]?.ifEmpty { null }

    /**
     * Writes [file] with exactly [keys], in order: names already chosen are kept, new keys get
     * an empty name, and keys the dump no longer has are dropped.
     */
    fun sync(file: Path, keys: Collection<String>): Int {
        val synced = keys.distinct().associateWith { JsonPrimitive(entries[it].orEmpty()) }
        file.writeText(PrettyJson.encodeToString(JsonObject.serializer(), JsonObject(synced)) + "\n")
        return synced.size
    }

    companion object {
        private val PrettyJson = Json { prettyPrint = true }

        fun load(file: Path): Overlay =
            if (file.exists()) Overlay(Json.parseToJsonElement(file.readText()).jsonObject.mapValues { it.value.jsonPrimitive.content })
            else Overlay(emptyMap())
    }
}
