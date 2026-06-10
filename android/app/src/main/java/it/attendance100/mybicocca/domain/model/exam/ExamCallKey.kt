package it.attendance100.mybicocca.domain.model.exam

/**
 * Canonical identity of an exam call inside Esse3: the triple that addresses every
 * appello endpoint (`/appelli/{cdsId}/{adId}/{appId}/...`). Shared by calls, bookings
 * and published results so the exam screens can correlate them.
 *
 * @property courseOfStudyId Esse3 `cdsId` of the course of study offering the call.
 * @property activityId Esse3 `adId` of the teaching activity being examined.
 * @property callId Esse3 `appId`, the call's progressive number within the activity.
 */
data class ExamCallKey(
    val courseOfStudyId: Long,
    val activityId: Long,
    val callId: Int,
)
