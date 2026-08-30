package it.attendance100.mybicocca.data.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Receives the outcome of a silent `PackageInstaller` session triggered for a nightly update.
 * Routes the result back to the active [ApkDownloader] singleton to update the UI state.
 */
@AndroidEntryPoint
class InstallResultReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var apkDownloader: ApkDownloader

    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        
        if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
            val confirmationIntent = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
            }
            if (confirmationIntent != null) {
                confirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(confirmationIntent)
                return
            }
        }
        
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
        apkDownloader.onInstallResult(status, message)
    }
}
