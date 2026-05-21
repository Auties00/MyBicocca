package it.attendance100.mybicocca.domain.model.elearning.message

enum class ConversationType(val code: Int) {
    Private(1),
    Group(2),
    Self(3),
    Unknown(0);

    companion object {
        fun fromCode(code: Int?): ConversationType =
            entries.firstOrNull { it.code == code } ?: Unknown
    }
}
