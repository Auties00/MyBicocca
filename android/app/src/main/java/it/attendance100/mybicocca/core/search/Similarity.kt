package it.attendance100.mybicocca.core.search

// Jaro-Winkler similarity in [0, 1]. Chosen over Levenshtein for the search box: it is
// prefix-weighted (users get the first characters right and typo mid-word) and returns a
// normalized score that folds directly into the matcher's tiers.
fun jaroWinkler(a: String, b: String): Double {
    if (a == b) return 1.0
    if (a.isEmpty() || b.isEmpty()) return 0.0
    val jaro = jaro(a, b)
    if (jaro == 0.0) return 0.0
    var prefix = 0
    val maxPrefix = minOf(4, a.length, b.length)
    while (prefix < maxPrefix && a[prefix] == b[prefix]) prefix++
    return jaro + prefix * 0.1 * (1 - jaro)
}

private fun jaro(a: String, b: String): Double {
    val window = (maxOf(a.length, b.length) / 2 - 1).coerceAtLeast(0)
    val aMatched = BooleanArray(a.length)
    val bMatched = BooleanArray(b.length)
    var matches = 0
    for (i in a.indices) {
        val from = (i - window).coerceAtLeast(0)
        val to = (i + window + 1).coerceAtMost(b.length)
        for (j in from until to) {
            if (!bMatched[j] && a[i] == b[j]) {
                aMatched[i] = true
                bMatched[j] = true
                matches++
                break
            }
        }
    }
    if (matches == 0) return 0.0
    var transpositions = 0
    var j = 0
    for (i in a.indices) {
        if (!aMatched[i]) continue
        while (!bMatched[j]) j++
        if (a[i] != b[j]) transpositions++
        j++
    }
    val m = matches.toDouble()
    return (m / a.length + m / b.length + (m - transpositions / 2) / m) / 3
}
