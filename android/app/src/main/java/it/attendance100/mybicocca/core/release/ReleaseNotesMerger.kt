package it.attendance100.mybicocca.core.release

/** A release's version paired with its parsed notes, the unit [mergeReleaseNotes] folds together. */
data class MergedReleaseSource(val version: String, val notes: ReleaseNotes)

/**
 * Folds several releases' notes into one combined changelog for the merged "What's New" view, so a
 * user sees everything new since their version grouped by category rather than as a stack of
 * per-release cards.
 *
 * [sources] are expected newest-first. Sections are matched across releases by their heading —
 * normalized so the emoji and spacing don't matter — and emitted in the project's canonical order
 * (New Features, Changes, Improvements, Fixes, Under the hood, Removals), with any unrecognized
 * headings kept after them in first-seen order. Each merged bullet and callout is stamped with the
 * version it came from (unless there is only one source, where the tag would be noise), and exact
 * duplicates are dropped.
 *
 * Intro paragraphs are discarded — that is how the "Check out the past release notes…" line
 * disappears in the merged view. Nothing is silently lost, though: a release that follows no
 * template (no headings at all) has its content gathered under an "Other changes" section — a
 * [ReleaseBlock.Heading] flagged [ReleaseBlock.Heading.isOtherChanges] so the renderer localizes
 * its label (this layer holds no resources) — and the full per-release notes always remain one tap
 * away in the "All versions" view. All callouts are kept (deduplicated) because a
 * [CalloutKind.WARNING]/[CalloutKind.CAUTION] can carry information worth not losing.
 */
fun mergeReleaseNotes(sources: List<MergedReleaseSource>): ReleaseNotes {
    if (sources.isEmpty()) return ReleaseNotes(emptyList())
    val tagVersions = sources.size > 1

    class Section(val heading: String, val items: MutableList<BulletItem> = mutableListOf())

    val sections = LinkedHashMap<String, Section>()
    val other = mutableListOf<BulletItem>()
    val callouts = mutableListOf<ReleaseBlock.Callout>()

    fun tag(version: String): String? = if (tagVersions) version else null

    for (source in sources) {
        val hasHeadings = source.notes.blocks.any { it is ReleaseBlock.Heading }
        var currentKey: String? = null

        for (block in source.notes.blocks) {
            when (block) {
                is ReleaseBlock.Heading -> {
                    currentKey = normalizeSectionKey(block.text)
                    sections.getOrPut(currentKey) { Section(block.text) }
                }

                is ReleaseBlock.BulletList -> {
                    val tagged = block.items.map { BulletItem(it.text, tag(source.version)) }
                    if (currentKey != null) sections.getValue(currentKey).items += tagged
                    else other += tagged // bullets before any heading
                }

                is ReleaseBlock.Paragraph ->
                    // Real content only when the release has no sections; otherwise it is the intro.
                    if (!hasHeadings) other += BulletItem(block.text, tag(source.version))

                is ReleaseBlock.Callout -> callouts += block.copy(version = tag(source.version))
            }
        }
    }

    val out = mutableListOf<ReleaseBlock>()

    sections.values
        .sortedBy { canonicalIndex(normalizeSectionKey(it.heading)) }
        .forEach { section ->
            val deduped = section.items.distinctBy { it.text }
            if (deduped.isNotEmpty()) {
                out += ReleaseBlock.Heading(MERGED_HEADING_LEVEL, section.heading)
                out += ReleaseBlock.BulletList(deduped)
            }
        }

    if (other.isNotEmpty()) {
        out += ReleaseBlock.Heading(MERGED_HEADING_LEVEL, text = "", isOtherChanges = true)
        out += ReleaseBlock.BulletList(other.distinctBy { it.text })
    }

    var seenDownloadTip = false
    val seen = HashSet<Pair<CalloutKind, String>>()
    callouts.forEach { callout ->
        // If multiple 'If you are unsure which version to download then go with...' Tip callouts are present, show only the latest
        val isDownloadTip = callout.kind == CalloutKind.TIP && callout.text.contains(
            "unsure which version to download",
            ignoreCase = true
        )

        if (isDownloadTip) {
            if (!seenDownloadTip) {
                out += callout
                seenDownloadTip = true
            }
        } else
            if (seen.add(callout.kind to callout.text)) {
                out += callout
            }
    }

    return ReleaseNotes(out)
}

/** Lowercases a heading and strips its leading emoji/whitespace so the same section matches across releases. */
private fun normalizeSectionKey(heading: String): String =
    heading.trim()
        .dropWhile { !it.isLetter() }
        .lowercase()
        .replace(MULTI_SPACE, " ")
        .trim()

/** Known sections sort by template order; unknown ones fall after, keeping their first-seen order. */
private fun canonicalIndex(key: String): Int =
    CANONICAL_SECTIONS.indexOf(key).let { if (it >= 0) it else CANONICAL_SECTIONS.size + 1 }

private const val MERGED_HEADING_LEVEL = 3

private val MULTI_SPACE = Regex("\\s+")

private val CANONICAL_SECTIONS = listOf(
    "new features",
    "changes",
    "improvements",
    "fixes",
    "under the hood",
    "removals",
)
