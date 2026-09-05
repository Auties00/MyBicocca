package it.attendance100.mybicocca.data.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import it.attendance100.mybicocca.data.notification.UpdateNotifications
import it.attendance100.mybicocca.domain.repository.UpdateRepository
import javax.inject.Inject

/**
 * Backs the Cancel action on the download notification.
 *
 * A receiver is the right shape here precisely because it does not open the app: cancelling work
 * from the shade should leave the user in the shade. The trampoline ban only forbids a receiver
 * that goes on to start an Activity, which this never does.
 *
 * It is reachable only because the manifest declares this action in an intent filter — the intent
 * is implicit and scoped to the package, and a filter-less receiver resolves to nothing.
 *
 * A partial file is left behind on purpose: it fails the size check on the next attempt and is
 * overwritten, which is cheaper than deleting a file that a retry seconds later would refetch.
 */
@AndroidEntryPoint
class CancelDownloadReceiver : BroadcastReceiver() {

    @Inject
    lateinit var updateRepository: UpdateRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != UpdateNotifications.ACTION_CANCEL_DOWNLOAD) return
        updateRepository.cancelDownload()
    }
}
