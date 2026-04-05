package it.attendance100.mybicocca.util.shared_transitions

data class CommonSharedElementKey(
    val elementId: Long,
    val type: CommonSharedElementType,
) : SharedElementKey {
    override val id: String = "common-$elementId-${type.name}"

    companion object {
        const val EXAM_SESSIONS_KEY: Long = 1
        const val STUDY_PLAN_KEY: Long = 2
        const val TAXES_KEY: Long = 3
        const val ISEE_KEY: Long = 4
    }
}

enum class CommonSharedElementType {
    Card,
    BackButton,
    BottomActionBar,
}
