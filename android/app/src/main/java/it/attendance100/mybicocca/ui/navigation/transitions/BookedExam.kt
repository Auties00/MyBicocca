package it.attendance100.mybicocca.ui.navigation.transitions

data class BookedExamElementKey(
    val infoPath: String,
    val type: BookedExamSharedElementType,
) : SharedElementKey {
    override val id: String = "bookedexam-$infoPath-${type.name}"
}

enum class BookedExamSharedElementType {
    Title,
    Description,
    Date,
    Body,
}