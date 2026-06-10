package it.attendance100.mybicocca.domain.model.account

/**
 * Moodle (elearning.unimib.it) side of a saved account, captured from the site-info call at
 * sign-in and cached in Room.
 *
 * @property lmsUserId Moodle user id; the `userid` parameter of the course, grade,
 *   badge, and assignment web-service calls.
 * @property lmsUsername Username as known to Moodle, typically the campus e-mail address.
 * @property locale Moodle language code of the user profile (e.g. "it", "en").
 * @property isSiteAdmin Whether Moodle reports the user as a site administrator; false for
 *   students.
 * @property maxUploadFileSizeBytes Per-file upload limit Moodle grants the user, in bytes;
 *   bounds assignment submission uploads.
 * @property storageQuotaBytes Private-files storage quota Moodle grants the user, in bytes.
 */
data class LearningIdentity(
    val lmsUserId: Int,
    val lmsUsername: String,
    val locale: String,
    val isSiteAdmin: Boolean,
    val maxUploadFileSizeBytes: Long,
    val storageQuotaBytes: Long,
)
