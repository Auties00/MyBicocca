package it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.state

/**
 * The in-sheet pager pages of the compito modal: [Detail] is the overview (root), [Compose] the
 * submission editor, [ConfirmSubmit] the finalize-for-grading confirmation.
 */
enum class AssignmentPage {
    Detail,
    Compose,
    ConfirmSubmit,
}

/**
 * A device file the user picked to hand in, identified by its content Uri. Carries display
 * metadata only; the ViewModel reads the bytes lazily, at save time, via ContentUriReader.
 */
data class PickedFile(
    val uri: String,
    val fileName: String,
    val mimeType: String?,
    val sizeBytes: Long?,
)
