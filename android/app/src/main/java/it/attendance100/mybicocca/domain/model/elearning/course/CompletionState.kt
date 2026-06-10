package it.attendance100.mybicocca.domain.model.elearning.course

import java.time.Instant

/**
 * Completion status of one course activity, sourced from the Moodle activity-completion
 * web service. Drives the per-module checkmarks and the progress ring on the course
 * detail screen.
 *
 * @property cmId The course-module id the status belongs to.
 * @property isCompleted Whether the activity counts as done (including done-with-pass).
 * @property completedAt When completion was recorded, or null if not completed or untimed.
 * @property isManual Whether the student ticks the activity off themselves.
 * @property isAutomatic Whether Moodle marks the activity based on conditions
 * (view, grade, submission).
 * @property isTracked Whether completion tracking is enabled for the activity at all;
 * untracked activities render without a completion affordance.
 */
data class CompletionState(
    val cmId: Int,
    val isCompleted: Boolean,
    val completedAt: Instant?,
    val isManual: Boolean,
    val isAutomatic: Boolean,
    val isTracked: Boolean,
)
