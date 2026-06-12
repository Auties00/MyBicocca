package it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes

import android.content.Context
import it.attendance100.mybicocca.R
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale

/**
 * Tuition amounts are always euro, but the grouping and symbol placement follow the app
 * language: the currency is pinned to EUR while the format tracks [Locale.getDefault].
 */
private val euroFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
    currency = Currency.getInstance("EUR")
}
private val taxDateFormat = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())

fun formatEuro(amount: Double): String = euroFormat.format(amount)

fun LocalDate.formatTaxDate(): String = format(taxDateFormat)

fun Throwable.taxFriendlyMessage(context: Context): String = when (this) {
    is UnknownHostException,
    is ConnectException -> context.getString(R.string.error_network_unavailable_full)

    is SocketTimeoutException -> context.getString(R.string.error_network_timeout_full)
    is IOException -> context.getString(R.string.error_network_generic_full)
    else -> context.getString(R.string.error_unexpected)
}
