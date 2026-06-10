package it.attendance100.mybicocca.domain.repository

/**
 * Access to the files attached to Moodle course modules. Files live behind tokenized
 * pluginfile URLs: a plain fetch without the web-service token is rejected, and an
 * expired token answers 200 with a JSON error body rather than an HTTP error.
 */
interface ElearningFileRepository {
    /**
     * Downloads the file behind a course-content fileUrl into app-private storage and
     * returns the absolute path of the local copy. Cached: a second call for the same
     * url + fileName returns the existing copy without hitting the network. Throws on
     * network/auth failure; the ViewModel translates to a sync status.
     */
    suspend fun downloadFile(fileUrl: String, fileName: String): String

    /**
     * Returns the fileUrl with the web service token appended, fetchable with a plain
     * GET by media players or external apps. The token grants whole-account API
     * access, so only hand this URL to consumers the user explicitly chose (e.g.
     * Office apps).
     */
    suspend fun authenticatedFileUrl(fileUrl: String): String
}
