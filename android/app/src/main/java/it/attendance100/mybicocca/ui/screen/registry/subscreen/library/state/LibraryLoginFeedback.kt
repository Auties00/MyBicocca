package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state

/**
 * Transient in-content feedback after tapping "Ho aperto il link": the login page animates to a
 * feedback panel and either reverts to the form ([LinkNotOpened]) or proceeds once acknowledged
 * ([LoggedIn]). These two outcomes render in-content rather than as result-page modals.
 */
enum class LibraryLoginFeedback { LinkNotOpened, LoggedIn }
