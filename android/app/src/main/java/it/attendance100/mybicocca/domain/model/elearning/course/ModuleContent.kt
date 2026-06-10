package it.attendance100.mybicocca.domain.model.elearning.course

import java.time.Instant

/**
 * One file or link bundled with a course module, from the `contents` array of the
 * Moodle course-contents web service. Feeds the file viewer and download flows.
 *
 * @property type Moodle content kind ("file", "url").
 * @property fileName Leaf file name as stored on Moodle.
 * @property filePath Moodle virtual path within the module ("/", "/tutorial/", ...).
 * Distinguishes an HTML entry point (index.html at "/") from its bundled image assets
 * in a deeper folder.
 * @property fileUrl The pluginfile download URL; requires the web-service token to fetch.
 * @property mimeType Declared MIME type, when known.
 * @property sizeBytes File size in bytes, when known.
 * @property timeModified Last modification time on the server, when known.
 */
data class ModuleContent(
    val type: String?,
    val fileName: String?,
    val filePath: String?,
    val fileUrl: String?,
    val mimeType: String?,
    val sizeBytes: Long?,
    val timeModified: Instant?,
)
