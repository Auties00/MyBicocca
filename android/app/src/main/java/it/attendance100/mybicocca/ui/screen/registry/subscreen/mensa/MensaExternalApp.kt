package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

private const val FRESH_AND_NATURAL_PACKAGE = "com.ristocloudgroup.freshandnatural"

/**
 * Opens the Fresh & Natural app, where the wallet is topped up, or its Play Store page when it is
 * not installed. Payments are deliberately not handled inside MyBicocca.
 */
internal fun Context.openFreshAndNatural() {
    val launch = packageManager.getLaunchIntentForPackage(FRESH_AND_NATURAL_PACKAGE)
    if (launch != null) {
        startActivity(launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        return
    }
    val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$FRESH_AND_NATURAL_PACKAGE"))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        startActivity(market)
    } catch (_: ActivityNotFoundException) {
        startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$FRESH_AND_NATURAL_PACKAGE"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}
