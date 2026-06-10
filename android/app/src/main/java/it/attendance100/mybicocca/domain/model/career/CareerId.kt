package it.attendance100.mybicocca.domain.model.career

/**
 * Identifier of a single career, taken from Esse3 (`stuId`). Esse3 endpoints that take a
 * `studentId` parameter expect this value. Also keys the career-scoped Room rows, such as
 * calendar events and transcript entries.
 */
@JvmInline
value class CareerId(val value: Long)
