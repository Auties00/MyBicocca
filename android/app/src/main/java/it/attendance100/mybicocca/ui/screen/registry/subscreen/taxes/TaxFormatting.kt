package it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val euroFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.ITALY)
private val taxDateFormat = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ITALIAN)

fun formatEuro(amount: Double): String = euroFormat.format(amount)

fun LocalDate.formatTaxDate(): String = format(taxDateFormat)

fun Throwable.taxFriendlyMessage(): String = when (this) {
    is UnknownHostException,
    is ConnectException -> "Rete non disponibile. Controlla la connessione e riprova."
    is SocketTimeoutException -> "Timeout di rete. Riprova tra un momento."
    is IOException -> "Errore di rete. Riprova tra un momento."
    else -> "Si è verificato un errore imprevisto"
}
