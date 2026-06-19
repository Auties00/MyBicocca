package it.attendance100.mybicocca.core.release

/**
 * A GitHub release body parsed into the handful of block kinds the "What's New" page renders.
 * Deliberately a small subset of Markdown — enough to recreate the project's release template
 * (intro line, emoji section headings, bullet lists, GitHub alert callouts) — with inline styling
 * left to the renderer. An [isEmpty] notes object (no recognizable blocks, e.g. an empty or
 * whitespace-only body) lets the card fall back to showing just its header.
 */
data class ReleaseNotes(val blocks: List<ReleaseBlock>) {
    val isEmpty: Boolean get() = blocks.isEmpty()
}

/** One block of a parsed release body. The text strings keep their raw inline Markdown. */
sealed interface ReleaseBlock {
    /** A Markdown heading; [level] is 1–6 (`#`..`######`). */
    data class Heading(val level: Int, val text: String) : ReleaseBlock

    /** A run of prose lines collapsed into one paragraph. */
    data class Paragraph(val text: String) : ReleaseBlock

    /** A bullet list. */
    data class BulletList(val items: List<BulletItem>) : ReleaseBlock

    /** A GitHub alert (`> [!TIP]` …); [text] is the body with its line breaks preserved. */
    data class Callout(val kind: CalloutKind, val text: String, val version: String? = null) :
        ReleaseBlock
}

/**
 * One bullet entry's raw inline Markdown, optionally stamped with the [version] it came from.
 * Per-release parsing leaves [version] null (the card header already states the version); the
 * merged "What's New" view sets it so each item can show a version chip.
 */
data class BulletItem(val text: String, val version: String? = null)

/** The GitHub alert kinds, each rendered with its own icon and accent colour. */
enum class CalloutKind { NOTE, TIP, IMPORTANT, WARNING, CAUTION }
