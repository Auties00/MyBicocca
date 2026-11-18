package it.attendance100.mybicocca.data.remote.easystaff.dto

/**
 * The request recording a student's presence at a lesson, with the fixed values the EasyBadge
 * app sends: no seat, English messages and an authenticated student.
 *
 * @param lessonCode The code shown in the room.
 * @param deviceToken An identifier of the device, which EasyBadge uses to spot shared devices.
 * @param timestampMillis When the presence was recorded, in milliseconds since the Unix epoch.
 */
fun EasyStaffCertifyAttendanceRequest(
    studentId: String,
    lessonCode: String,
    deviceToken: String,
    latitude: Double,
    longitude: Double,
    timestampMillis: Long = System.currentTimeMillis(),
): EasyStaffCertifyAttendanceRequest = EasyStaffCertifyAttendanceRequest(
    studentId = studentId,
    lessonCode = lessonCode,
    deviceToken = deviceToken,
    seat = "",
    language = "en",
    isAuthenticated = true,
    action = "CreateFromJSON",
    additionalData = EasyStaffCertifyAttendanceAdditionalData(timestamp = timestampMillis, longitude = longitude, latitude = latitude),
)
