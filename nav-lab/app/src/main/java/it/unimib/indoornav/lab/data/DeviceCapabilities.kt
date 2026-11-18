package it.unimib.indoornav.lab.data

import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import androidx.core.location.LocationManagerCompat
import it.unimib.indoornav.lab.format.CapabilitiesInfo
import it.unimib.indoornav.lab.format.PhoneInfo
import it.unimib.indoornav.lab.format.SensorManifestEntry
import kotlin.math.max

data class SensorDescriptor(
    val androidType: Int,
    val manifestType: String,
    val targetPeriodUs: Int,
)

data class SensorCapabilityItem(
    val manifestType: String,
    val present: Boolean,
    val name: String,
    val vendor: String,
    val minDelayUs: Int,
    val resolution: Float,
    val requestedPeriodUs: Int,
) {
    val maxEffectiveHz: Double
        get() = if (requestedPeriodUs > 0) 1_000_000.0 / requestedPeriodUs else 0.0
}

enum class WifiThrottleCheckState {
    UNTESTED,
    TESTING,
    OFF,
    ON,
    FAILED,
}

data class ReadinessStatus(
    val bluetoothOn: Boolean,
    val locationOn: Boolean,
    val wifiEnabled: Boolean,
    val wifiThrottleState: WifiThrottleCheckState,
    val wifiThrottleDetail: String,
    val batteryOptimizationExempt: Boolean,
    val batteryPercent: Int,
    val freeStorageGb: Double,
    val permissionsGranted: Boolean,
    val missingPermissions: List<String>,
)

data class DeviceCapabilitySnapshot(
    val sensors: List<SensorCapabilityItem>,
    val capabilities: CapabilitiesInfo,
) {
    fun toManifestSensors(): List<SensorManifestEntry> =
        sensors.filter { it.present && it.requestedPeriodUs > 0 }.map {
            SensorManifestEntry(
                type = it.manifestType,
                name = it.name,
                vendor = it.vendor,
                min_delay_us = it.minDelayUs,
                resolution = it.resolution,
                requested_period_us = it.requestedPeriodUs,
            )
        }
}

object DeviceCapabilities {
    const val MIN_ALLOWED_PERIOD_US = 5_000

    val RECORDED_SENSORS = listOf(
        SensorDescriptor(Sensor.TYPE_ACCELEROMETER, "accelerometer", 10_000),
        SensorDescriptor(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED, "accelerometer_uncalibrated", 10_000),
        SensorDescriptor(Sensor.TYPE_GYROSCOPE, "gyroscope", 10_000),
        SensorDescriptor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED, "gyroscope_uncalibrated", 10_000),
        SensorDescriptor(Sensor.TYPE_MAGNETIC_FIELD, "magnetic_field", 20_000),
        SensorDescriptor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED, "magnetic_field_uncalibrated", 20_000),
        SensorDescriptor(Sensor.TYPE_PRESSURE, "pressure", 40_000),
        SensorDescriptor(Sensor.TYPE_GAME_ROTATION_VECTOR, "game_rotation_vector", 20_000),
        SensorDescriptor(Sensor.TYPE_ROTATION_VECTOR, "rotation_vector", 20_000),
        SensorDescriptor(Sensor.TYPE_STEP_DETECTOR, "step_detector", 0),
        SensorDescriptor(Sensor.TYPE_STEP_COUNTER, "step_counter", 0),
    )

    fun effectivePeriodUs(targetPeriodUs: Int, sensor: Sensor): Int {
        if (targetPeriodUs <= 0) return 0
        return max(MIN_ALLOWED_PERIOD_US, max(targetPeriodUs, sensor.minDelay))
    }

    fun inspectCapabilities(context: Context): DeviceCapabilitySnapshot {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val items = RECORDED_SENSORS.map { desc ->
            val sensor = sensorManager.getDefaultSensor(desc.androidType)
            if (sensor != null) {
                SensorCapabilityItem(
                    manifestType = desc.manifestType,
                    present = true,
                    name = sensor.name,
                    vendor = sensor.vendor,
                    minDelayUs = sensor.minDelay,
                    resolution = sensor.resolution,
                    requestedPeriodUs = effectivePeriodUs(desc.targetPeriodUs, sensor),
                )
            } else {
                SensorCapabilityItem(
                    manifestType = desc.manifestType,
                    present = false,
                    name = "Not present",
                    vendor = "",
                    minDelayUs = 0,
                    resolution = 0f,
                    requestedPeriodUs = desc.targetPeriodUs,
                )
            }
        }

        val pm = context.packageManager
        val hasBaro = items.any { it.manifestType == "pressure" && it.present }
        val hasGyro = items.any { it.manifestType == "gyroscope" && it.present }
        val hasWifiRtt = pm.hasSystemFeature(PackageManager.FEATURE_WIFI_RTT)
        // No device reports a feature flag for ARCore; it is available when Google Play Services for AR is installed.
        val hasArCore = runCatching { pm.getPackageInfo("com.google.ar.core", 0) }.isSuccess

        return DeviceCapabilitySnapshot(
            sensors = items,
            capabilities = CapabilitiesInfo(
                barometer = hasBaro,
                gyroscope = hasGyro,
                wifi_rtt = hasWifiRtt,
                arcore = hasArCore,
            ),
        )
    }

    fun phoneInfo(label: String): PhoneInfo = PhoneInfo(
        label = label.ifBlank { "A1" },
        model = Build.MODEL ?: "Unknown",
        os = Build.VERSION.RELEASE ?: Build.VERSION.SDK_INT.toString(),
        manufacturer = Build.MANUFACTURER ?: "Unknown",
    )

    fun readReadiness(
        context: Context,
        missingPermissions: List<String>,
        wifiThrottleState: WifiThrottleCheckState,
        wifiThrottleDetail: String,
    ): ReadinessStatus {
        val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val btOn = btManager?.adapter?.isEnabled == true

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationOn = LocationManagerCompat.isLocationEnabled(locationManager)

        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val wifiEnabled = wifiManager?.isWifiEnabled == true

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val batteryExempt = powerManager.isIgnoringBatteryOptimizations(context.packageName)

        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val batteryPct = if (level >= 0 && scale > 0) (level * 100) / scale else 0

        val storageDir = context.getExternalFilesDir(null) ?: Environment.getDataDirectory()
        val stat = StatFs(storageDir.absolutePath)
        val freeBytes = stat.availableBytes
        val freeGb = freeBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)

        return ReadinessStatus(
            bluetoothOn = btOn,
            locationOn = locationOn,
            wifiEnabled = wifiEnabled,
            wifiThrottleState = wifiThrottleState,
            wifiThrottleDetail = wifiThrottleDetail,
            batteryOptimizationExempt = batteryExempt,
            batteryPercent = batteryPct,
            freeStorageGb = freeGb,
            permissionsGranted = missingPermissions.isEmpty(),
            missingPermissions = missingPermissions,
        )
    }
}
