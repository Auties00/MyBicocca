package it.unimib.indoornav.lab.capture

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.SystemClock
import androidx.core.content.ContextCompat
import it.unimib.indoornav.lab.format.StreamSpec

class GnssCapture(
    private val context: Context,
    private val looper: Looper,
    private val writer: SessionWriter,
) : StreamCapture, LocationListener {

    override val streamKey: String = StreamSpec.GNSS.streamKey

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    @Volatile
    private var started = false

    @SuppressLint("MissingPermission")
    override fun start(): Boolean {
        if (started) return true
        val lm = locationManager ?: return false
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        val provider = selectProvider(lm) ?: return false
        return runCatching {
            lm.requestLocationUpdates(provider, 1_000L, 0f, this, looper)
            started = true
            writer.openStream(StreamSpec.GNSS)
            true
        }.getOrElse { t ->
            writer.recordError("GNSS start failed: ${t.message}")
            started = false
            false
        }
    }

    override fun stop() {
        if (!started) return
        started = false
        runCatching { locationManager?.removeUpdates(this) }
    }

    override fun onLocationChanged(location: Location) {
        val rxNs = SystemClock.elapsedRealtimeNanos()
        val tNs = location.elapsedRealtimeNanos
        val acc = if (location.hasAccuracy()) location.accuracy else ""
        val alt = if (location.hasAltitude()) location.altitude else ""
        writer.writeRow(
            StreamSpec.GNSS,
            tNs,
            rxNs,
            tNs,
            rxNs,
            location.latitude,
            location.longitude,
            acc,
            alt,
            location.provider ?: "",
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}

    private fun selectProvider(lm: LocationManager): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && lm.allProviders.contains(LocationManager.FUSED_PROVIDER)) {
            return LocationManager.FUSED_PROVIDER
        }
        return when {
            lm.allProviders.contains(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            lm.allProviders.contains(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> null
        }
    }
}
