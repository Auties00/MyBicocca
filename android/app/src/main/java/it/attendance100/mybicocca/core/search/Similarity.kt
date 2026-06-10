package it.attendance100.mybicocca.core.search

/**
 * Jaro-Winkler similarity in 0..1.
 *
 * Chosen over plain edit distance for the search box because it is prefix-weighted: the
 * Winkler bonus rewards up to four leading characters in common, matching how users type
 * (first characters right, typos mid-word). The normalized score folds directly into
 * [SearchMatcher]'s typo tier, where it ranks candidates that already passed the
 * edit-distance admission gate. O(|a|·|b|) time.
 */
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

/**
 * Damerau-Levenshtein distance (optimal string alignment variant) capped at [max]: returns
 * -1 as soon as the distance is provably above the cap, via a length-difference pre-check
 * and a per-row minimum cutoff. The adjacent transposition ("hte" -> "the") counts as a
 * single edit — the most common real typo class.
 *
 * Serves as the admission gate of [SearchMatcher]'s typo tier: an integer edit budget keyed
 * on token length behaves predictably across lengths, where a single Jaro-Winkler floor is
 * too lax on short tokens and too strict on long ones. O(|a|·|b|) time, O(|b|) space — the
 * three DP rows are recycled.
 */
fun damerauLevenshtein(a: String, b: String, max: Int): Int {
    if (a == b) return 0
    if (max <= 0) return -1
    val lenDiff = a.length - b.length
    if (lenDiff > max || -lenDiff > max) return -1
    var prevPrev: IntArray? = null
    var prev = IntArray(b.length + 1) { it }
    var current = IntArray(b.length + 1)
    for (i in 1..a.length) {
        current[0] = i
        var rowMin = i
        for (j in 1..b.length) {
            val cost = if (a[i - 1] == b[j - 1]) 0 else 1
            var d = minOf(prev[j] + 1, current[j - 1] + 1, prev[j - 1] + cost)
            val pp = prevPrev
            if (pp != null && i > 1 && j > 1 && a[i - 1] == b[j - 2] && a[i - 2] == b[j - 1]) {
                d = minOf(d, pp[j - 2] + 1)
            }
            current[j] = d
            if (d < rowMin) rowMin = d
        }
        if (rowMin > max) return -1
        val recycled = prevPrev ?: IntArray(b.length + 1)
        prevPrev = prev
        prev = current
        current = recycled
    }
    return prev[b.length].takeIf { it <= max } ?: -1
}

/**
 * Jaro similarity: characters match when equal and within a window of half the longer
 * string's length; the score averages the match ratio of each string with a transposition
 * discount over the matched characters. Returns 0 when nothing matches.
 */
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
