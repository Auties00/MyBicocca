package it.attendance100.mybicocca.core.utils

import com.ibm.icu.text.RuleBasedNumberFormat
import java.util.Locale

// extend String class to implement a function that returns the string with the first letter capitalized
fun String.capitalizeString(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

fun Int.toOrdinal(locale: Locale = Locale.getDefault()): String {
    val formatter = RuleBasedNumberFormat(locale, RuleBasedNumberFormat.ORDINAL)
    return formatter.format(this)
}