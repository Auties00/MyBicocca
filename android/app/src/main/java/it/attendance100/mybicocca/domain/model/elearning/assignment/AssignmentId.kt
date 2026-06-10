package it.attendance100.mybicocca.domain.model.elearning.assignment

/**
 * Identifier of an assignment (compito) on the e-learning platform — Moodle's mod_assign
 * instance id, distinct from the course-module id (cmid) of the same activity.
 *
 * @property value Positive Moodle assignment instance id.
 */
@JvmInline
value class AssignmentId(val value: Int)
