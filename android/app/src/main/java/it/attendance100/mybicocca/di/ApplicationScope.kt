package it.attendance100.mybicocca.di

import javax.inject.Qualifier

/**
 * Qualifies the application-lifetime coroutine scope (a supervisor job on the default
 * dispatcher) used for work that must outlive any single screen, such as session reconciliation
 * and process-wide observers like the app-lock gate.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
