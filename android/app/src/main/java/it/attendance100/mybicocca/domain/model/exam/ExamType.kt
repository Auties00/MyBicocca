package it.attendance100.mybicocca.domain.model.exam

// Exam mode defined on the exam call. Joint = one grade for both parts,
// Separate = the written and oral parts are graded as distinct calls.
enum class ExamType {
    Written,
    Oral,
    WrittenAndOralJoint,
    WrittenAndOralSeparate,
    Unknown,
}
