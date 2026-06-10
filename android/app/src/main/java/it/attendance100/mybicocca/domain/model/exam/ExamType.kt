package it.attendance100.mybicocca.domain.model.exam

/**
 * Exam mode defined on the exam call, from Esse3 `tipoEsaCod` ("S", "O", "SOC", "SOS").
 * [WrittenAndOralJoint] means one grade covers both parts; [WrittenAndOralSeparate]
 * means the written and oral parts are graded as distinct calls. [Unknown] covers any
 * unrecognised code.
 */
enum class ExamType {
    Written,
    Oral,
    WrittenAndOralJoint,
    WrittenAndOralSeparate,
    Unknown,
}
