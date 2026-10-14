package it.attendance100.mybicocca.domain.model.update

/**
 * Default periodic-check interval, in minutes, until the user picks one via the settings slider.
 * One of the slider's steps. Kept well clear of GitHub's unauthenticated limit of 60 requests an
 * hour per IP, which students behind the campus NAT share.
 */
const val DEFAULT_UPDATE_CHECK_INTERVAL_MINUTES = 360
