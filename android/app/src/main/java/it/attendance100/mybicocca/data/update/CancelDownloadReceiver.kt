package it.attendance100.mybicocca.data.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import it.attendance100.mybicocca.core.notification.NotificationId
import it.attendance100.mybicocca.data.notification.AppNotifier
import it.attendance100.mybicocca.data.notification.UpdateNotifications
import javax.inject.Inject

/**
 * Backs the Cancel action on the download notification.
 *
 * A receiver is the right shape here precisely because it does not open the app: cancelling work
 * from the shade should leave the user in the shade. The trampoline ban only forbids a receiver
 * that goes on to start an Activity, which this never does.
 *
 * Cancelling the worker is what stops the download — it cancels the coroutine `download` runs in.
 * A partial file is left behind on purpose: it fails the size check on the next attempt and is
 * overwritten, which is cheaper than deleting a file that a retry seconds later would refetch.
 */
@AndroidEntryPoint
class CancelDownloadReceiver : BroadcastReceiver() {

    @Inject
    lateinit var downloader: ApkDownloader

    @Inject
    lateinit var notifier: AppNotifier

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != UpdateNotifications.ACTION_CANCEL_DOWNLOAD) return

        WorkManager.getInstance(context).cancelUniqueWork(ApkDownloadWorker.UNIQUE_WORK_NAME)
        downloader.resetState()
        notifier.cancel(NotificationId.UpdateProgress)
    }
}
