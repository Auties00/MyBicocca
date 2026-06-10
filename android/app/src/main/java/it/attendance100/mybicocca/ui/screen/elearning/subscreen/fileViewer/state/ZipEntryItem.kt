package it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.state

/** One extractable file of an opened zip archive, as listed by the zip viewer. */
data class ZipEntryItem(
    /** Full path inside the archive, e.g. `src/Main.java` — the extraction key. */
    val entryPath: String,
    /** Leaf name shown in the list, e.g. `Main.java`. */
    val displayName: String,
    val sizeBytes: Long,
)
