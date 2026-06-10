package it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum.state

/**
 * Server-side paging state for the discussions list: how many pages are already in the cache,
 * whether the server may have more (a full page implies more), and whether a load-more request
 * is in flight so the list can show its tail spinner and avoid duplicate requests.
 */
data class PageState(
    val loadedPages: Int = 0,
    val hasMore: Boolean = true,
    val isLoadingMore: Boolean = false,
)
