package it.attendance100.mybicocca.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object UiFormatter {
	private fun dayFormatter(locale: Locale) = DateTimeFormatter.ofPattern("d", locale)
	private fun monthFormatter(locale: Locale) = DateTimeFormatter.ofPattern("MMM", locale)
	private fun timeFormatter(locale: Locale) = DateTimeFormatter.ofPattern("HH:mm", locale)
	private fun fullDateFormatter(locale: Locale) = DateTimeFormatter.ofPattern("d MMMM yyyy", locale)
    private fun customDateFormatter(locale: Locale, pattern: String) =
        DateTimeFormatter.ofPattern(pattern, locale)

	fun getDay(date: LocalDateTime, locale: Locale): String = date.format(dayFormatter(locale))
	fun getMonth(date: LocalDateTime, locale: Locale): String = date.format(monthFormatter(locale)).uppercase()
	fun getTime(date: LocalDateTime, locale: Locale): String = date.format(timeFormatter(locale))
	fun getFullDate(date: LocalDateTime, locale: Locale): String = date.format(fullDateFormatter(locale))
    fun getCustomDate(date: LocalDateTime, locale: Locale, pattern: String): String =
        date.format(customDateFormatter(locale, pattern))


	fun getDay(data: LocalDate, locale: Locale): String = data.format(dayFormatter(locale))
	fun getMonth(data: LocalDate, locale: Locale): String = data.format(monthFormatter(locale)).uppercase()
	fun getTime(data: LocalDate, locale: Locale): String = data.format(timeFormatter(locale))
	fun getFullDate(data: LocalDate, locale: Locale): String = data.format(fullDateFormatter(locale))
    fun getCustomDate(data: LocalDate, locale: Locale, pattern: String): String =
        data.format(customDateFormatter(locale, pattern))
}

fun LocalTime.milli(): Int = this.nano / 1_000_000
fun LocalDateTime.milli(): Int = this.nano / 1_000_000
