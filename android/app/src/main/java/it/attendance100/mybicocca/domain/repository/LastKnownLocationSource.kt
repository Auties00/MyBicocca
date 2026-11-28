package it.attendance100.mybicocca.domain.repository

/** Best-effort reader for the device's last known coordinates without requesting permission. */
interface LastKnownLocationSource {
    fun lastKnownLatLong(): Pair<Double, Double>?
}
