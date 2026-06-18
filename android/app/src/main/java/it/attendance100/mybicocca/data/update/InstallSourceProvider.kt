package it.attendance100.mybicocca.data.update

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import it.attendance100.mybicocca.domain.model.update.DistributionSource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves how this build was installed by reading the OS install-source. On API 30+ the
 * non-deprecated [PackageManager.getInstallSourceInfo] is used; below it the older
 * [PackageManager.getInstallerPackageName] still works (querying your own package needs no
 * `<queries>` entry). A Play install reports the Play Store package; anything else — a sideloaded
 * APK from the GitHub release page, an `adb install`, a file-manager install — reports null or a
 * different installer and is treated as [DistributionSource.GITHUB].
 *
 * Failures are swallowed to GITHUB: not knowing the source must never block the update flow.
 */
@Singleton
class InstallSourceProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun resolve(): DistributionSource {
        val installer = runCatching { installerPackageName() }.getOrNull()
        return if (installer == PLAY_STORE_PACKAGE) {
            DistributionSource.PLAY_STORE
        } else {
            DistributionSource.GITHUB
        }
    }

    @Suppress("DEPRECATION")
    private fun installerPackageName(): String? {
        val pm = context.packageManager
        val self = context.packageName
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            pm.getInstallSourceInfo(self).installingPackageName
        } else {
            pm.getInstallerPackageName(self)
        }
    }

    private companion object {
        const val PLAY_STORE_PACKAGE = "com.android.vending"
    }
}
