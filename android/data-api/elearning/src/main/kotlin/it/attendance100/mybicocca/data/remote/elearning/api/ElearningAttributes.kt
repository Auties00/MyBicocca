package it.attendance100.mybicocca.data.remote.elearning.api

import io.ktor.util.AttributeKey

object ElearningAttributes {
    val SkipCookies = AttributeKey<Unit>("SkipCookies")

    /**
     * Client-level attribute holding the Moodle langpack code (e.g. `"it"`, `"en"`) sent
     * as `moodlewssettinglang` with every web service request. Stored on the shared
     * [io.ktor.client.HttpClient] so all API classes see the same value without each
     * having to carry it. Configure it via [ElearningApi.language]; when absent,
     * [ElearningAbstractApi.DEFAULT_LANGUAGE] is used.
     */
    val Language = AttributeKey<String>("ElearningLanguage")
}
