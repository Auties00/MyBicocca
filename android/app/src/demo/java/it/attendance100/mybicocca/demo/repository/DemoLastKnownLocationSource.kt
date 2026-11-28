package it.attendance100.mybicocca.demo.repository

import it.attendance100.mybicocca.domain.repository.LastKnownLocationSource
import javax.inject.Inject
import javax.inject.Singleton

/** Demo location source returning null so screenshots never depend on host GPS state. */
@Singleton
class DemoLastKnownLocationSource @Inject constructor() : LastKnownLocationSource {
    override fun lastKnownLatLong(): Pair<Double, Double>? = null
}
