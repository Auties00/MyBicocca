package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state

import it.attendance100.mybicocca.core.state.Loadable

/**
 * Display model for the hero's continue-watching card: the chosen video's title, owning
 * section subtitle, watch fraction and elapsed/total time labels. [thumbnail] is NotYetLoaded
 * while the thumbnail URL is still being resolved (show the loading state by default), and
 * Loaded(null) when there is genuinely no thumbnail (show the no-image state).
 */
data class ContinuePlayable(
    val title: String,
    val subtitle: String?,
    val progress: Float,
    val elapsedLabel: String?,
    val totalLabel: String?,
    val thumbnail: Loadable<String?>,
)
