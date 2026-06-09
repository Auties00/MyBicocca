package it.attendance100.mybicocca.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    // Best-effort last known coordinates (lat, long); null without permission or fix.
    // EasyBadge certification still proceeds without it — the server validates the code.
    @SuppressLint("MissingPermission")
    fun lastKnownLatLong(): Pair<Double, Double>? {
        if (!hasPermission()) return null
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        return PROVIDERS
            .mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull { it.time }
            ?.let { it.latitude to it.longitude }
    }

    private fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    private companion object {
        val PROVIDERS = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
        )
    }
}
