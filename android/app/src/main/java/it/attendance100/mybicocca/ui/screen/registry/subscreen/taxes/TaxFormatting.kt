package it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes

import androidx.compose.runtime.Composable
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.currentLocale
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ofPattern
import java.util.Currency
import java.util.Locale

/**
 * Tuition amounts are always euro, but the grouping and symbol placement follow the app
 * language: the currency is pinned to EUR while the format tracks [Locale.getDefault].
 */
@get:Composable
private val euroFormat: NumberFormat
    get() = NumberFormat.getCurrencyInstance(currentLocale()).apply {
        currency = Currency.getInstance("EUR")
    }

@get:Composable
private val taxDateFormat
    get() = ofPattern("d MMM yyyy", currentLocale())

@Composable
fun formatEuro(amount: Double): String = euroFormat.format(amount)

@Composable
fun LocalDate.formatTaxDate(): String = format(taxDateFormat)

fun Throwable.taxFriendlyMessage(): Int = when (this) {
    is UnknownHostException, is ConnectException -> R.string.error_network_unavailable_full
    is SocketTimeoutException -> R.string.error_network_timeout_full
    is IOException -> R.string.error_network_generic_full
    else -> R.string.error_unexpected
}
