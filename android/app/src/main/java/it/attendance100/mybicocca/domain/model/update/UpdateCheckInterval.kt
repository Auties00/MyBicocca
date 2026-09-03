package it.attendance100.mybicocca.domain.model.update

/**
 * Default periodic-check interval, in minutes, until the user picks one via the settings slider.
 * Currently kept low (WorkManager's own 15-minute floor) while the update-notifications work is
 * being tested; expected to move up to something like 6-12 hours once that settles.
 */
const val DEFAULT_UPDATE_CHECK_INTERVAL_MINUTES = 15
