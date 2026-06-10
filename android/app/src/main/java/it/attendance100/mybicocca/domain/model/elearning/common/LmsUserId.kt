package it.attendance100.mybicocca.domain.model.elearning.common

/**
 * Identifier of a user on the Moodle e-learning platform, distinct from the Esse3
 * student id of the same person.
 *
 * @property value The numeric Moodle user id, used as the `userid` parameter of
 * user-scoped web-service calls (courses, grades, badges).
 */
@JvmInline
value class LmsUserId(val value: Int)
