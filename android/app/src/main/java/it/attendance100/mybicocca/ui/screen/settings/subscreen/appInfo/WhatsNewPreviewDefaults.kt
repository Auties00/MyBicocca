package it.attendance100.mybicocca.ui.screen.settings.subscreen.appInfo

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import it.attendance100.mybicocca.core.release.MergedReleaseSource
import it.attendance100.mybicocca.core.release.mergeReleaseNotes
import it.attendance100.mybicocca.core.release.parseReleaseNotes
import it.attendance100.mybicocca.domain.model.update.AppRelease
import java.time.Instant

/*
 * Preview-only fixtures for the "What's New" page, kept out of WhatsNewSheet.kt because they are
 * bulky and shared across several @Preview composables there (per-release card, merged entry, the
 * no-merge entry, and the all-versions list) — matching the project's *PreviewDefaults convention
 * of colocating shared preview data in the same package as the previews that use it. Nothing here
 * is referenced by runtime code.
 */

/**
 * Mock release bodies covering the full range the parser/renderer must handle: the complete
 * template, partial section sets, a single section, a sectionless maintenance note, every callout
 * kind (TIP/WARNING/NOTE/IMPORTANT/CAUTION), the inline styles (bold/italic/strike/underline/code,
 * links and @mentions), and an empty body that collapses to just the card header. They run through
 * the real [parseReleaseNotes]/[mergeReleaseNotes].
 */
private data class MockRelease(
    val version: String,
    val daysAgo: Long,
    val body: String,
)

private val MOCK_RELEASES = listOf(
    MockRelease(
        "0.1.0", 1,
        """
        Check out the [past release notes](https://github.com/Auties00/MyBicocca/releases) if you're upgrading from an earlier version.

        ### ✨ New Features
        - Brand-new **campus map** with live room availability
        - Dark theme now follows the *system* setting

        ### ⚙️ Changes
        - Faster login flow
        - Reworked the `Libretto` screen

        ### 🖌️ Improvements
        - Smoother animations across the app (@LordLux)
        - Better offline handling for exams

        ### 🧩 Fixes
        - Fixed a crash when opening ~~assignments~~ quizzes
        - Corrected the GPA calculation

        ### 🪛 Under the hood
        - Upgraded to Kotlin 2.3 and migrated to Navigation 3

        ### 🧹 Removals
        - Dropped the legacy <u>web view</u> player

        > [!TIP]
        > If you are unsure which version to download then go with `app-universal-release.apk`
        """.trimIndent(),
    ),
    MockRelease(
        "0.0.6", 4,
        """
        ### ✨ New Features
        - Added pull-to-refresh on the calendar

        ### 🧩 Fixes
        - Fixed wrong exam dates in some edge cases

        > [!WARNING]
        > This build resets your saved filters — you'll need to set them again.
        """.trimIndent(),
    ),
    MockRelease(
        "0.0.5", 9,
        """
        ### 🧩 Fixes
        - Fixed login on slow networks
        - Fixed a layout glitch on small screens
        - Fixed the **What's New** page not scrolling
        """.trimIndent(),
    ),
    MockRelease(
        "0.0.4", 15,
        """
        A small maintenance release with no user-facing changes.

        > [!NOTE]
        > Internal tweaks only — nothing to see here.
        """.trimIndent(),
    ),
    MockRelease(
        "0.0.3", 21,
        """
        ### ⚙️ Changes
        - The home screen now shows your *next* lesson first

        ### 🖌️ Improvements
        - Search is now `typo-tolerant` and much faster

        > [!IMPORTANT]
        > The minimum supported Android version is now **8.0**.
        """.trimIndent(),
    ),
    MockRelease(
        "0.0.2", 27,
        """
        ### ✨ New Features
        - Library seat booking via Affluences

        ### 🪛 Under the hood
        - Switched the HTTP client to Ktor 3

        ### 🧹 Removals
        - Removed the old notifications service

        > [!CAUTION]
        > Booking a seat requires location permission.
        """.trimIndent(),
    ),
    MockRelease("0.0.1", 33, ""),
)

private fun mockReleaseItems(): List<ReleaseItem> = MOCK_RELEASES.map { mock ->
    ReleaseItem(
        release = AppRelease(
            versionName = mock.version,
            title = mock.version,
            notes = mock.body,
            pageUrl = "https://github.com/Auties00/MyBicocca/releases/tag/${mock.version}",
            publishedAt = Instant.now().minusSeconds(mock.daysAgo * 86_400L),
            isPreRelease = false,
        ),
        notes = parseReleaseNotes(mock.body),
    )
}

/** Feeds one mock [ReleaseItem] per preview into the per-card `@PreviewParameter` previews. */
internal class ReleaseItemProvider : PreviewParameterProvider<ReleaseItem> {
    override val values = mockReleaseItems().asSequence()
}

/** A Loaded state built from the mocks: every mock merged into one changelog, plus the full list. */
internal fun mockLoadedState(): WhatsNewUiState.Loaded {
    val items = mockReleaseItems()
    val merged = mergeReleaseNotes(
        sources = items.map { MergedReleaseSource(it.release.versionName, it.notes) },
        otherLabel = "Other changes",
    )
    val latest = items.first()
    return WhatsNewUiState.Loaded(
        merged = MergedSummary(
            latestVersion = latest.release.versionName,
            sinceVersion = items.last().release.versionName,
            latestPublishedAt = latest.release.publishedAt,
            latestPageUrl = latest.release.pageUrl,
            notes = merged,
            mergedVersionCount = items.size,
        ),
        releases = items,
    )
}
