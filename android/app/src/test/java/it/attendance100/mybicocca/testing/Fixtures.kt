package it.attendance100.mybicocca.testing

import kotlinx.serialization.json.Json

/**
 * Loads recorded payloads from the test classpath (`src/test/resources/`), for the handful of
 * mapper tests that are best pinned against a real captured server response rather than a
 * hand-built DTO. Prefer constructing DTOs directly in-test when the shape is small; reach for a
 * fixture only when the payload's realism (odd nulls, sentinel values, ordering) is the point.
 *
 * Example: `Fixtures.text("fixtures/esse3/bookings.json")`.
 */
object Fixtures {

    /**
     * Lenient [Json] mirroring the data-api clients' tolerance: unknown keys are ignored and
     * absent optional fields fall back to defaults, so a captured payload keeps deserializing
     * when the university adds fields.
     */
    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    /** Raw UTF-8 text of a classpath resource; fails loudly with the path when it is missing. */
    fun text(path: String): String =
        requireNotNull(javaClass.classLoader?.getResourceAsStream(path)) {
            "Fixture not found on the test classpath: $path"
        }.bufferedReader().use { it.readText() }

    /** [text] deserialized into [T] with the lenient [json]. */
    inline fun <reified T> decode(path: String): T = json.decodeFromString(text(path))
}
