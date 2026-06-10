package it.attendance100.mybicocca.domain.model.enrollment

/**
 * Identity of one annual enrollment row. Esse3 does not always expose a dedicated row
 * id, so the value falls back from the enrollment id to the student's `matId` to the
 * academic year — it is unique within a single career's history, not globally.
 *
 * @property value The raw Esse3-derived identifier.
 */
@JvmInline
value class EnrollmentId(val value: Long)
