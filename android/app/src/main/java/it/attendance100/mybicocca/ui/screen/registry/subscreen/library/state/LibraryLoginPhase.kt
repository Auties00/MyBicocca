package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state

// Steps of the email-validation login: enter the email, wait for the link request, wait for the
// user to open the emailed link, then verify (poll) it.
enum class LibraryLoginPhase { EnterEmail, Sending, AwaitingClick, Verifying }
