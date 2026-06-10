package it.attendance100.mybicocca.domain.model.settings

/**
 * How a viewable e-learning file kind (image, video, audio, html, text, zip…) should be opened:
 * [InApp] renders it with the built-in viewer, [External] hands it to another installed app.
 *
 * App-local preference remembered per file kind in the shared settings DataStore. A kind with
 * no remembered choice means "ask every time": the file-open chooser sheet is shown, and the
 * Associazioni file settings page can clear a remembered choice back to that state.
 */
enum class FileOpenChoice { InApp, External }
