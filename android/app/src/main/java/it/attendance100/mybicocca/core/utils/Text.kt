package it.attendance100.mybicocca.core.utils

// extend String class to implement a function that returns the string with the first letter capitalized
fun String.capitalizeString(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}