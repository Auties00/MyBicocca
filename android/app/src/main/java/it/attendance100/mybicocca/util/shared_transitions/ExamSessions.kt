package it.attendance100.mybicocca.util.shared_transitions

data class ExamSessionsElementKey(
    val infoPath: String,
    val type: ExamSessionSharedElementType,
) : SharedElementKey {
    override val id: String = "examsession-$infoPath-${type.name}"
}

enum class ExamSessionSharedElementType {
    Title,
    Card,
    Description,
    DateTimeBuilding,
}
