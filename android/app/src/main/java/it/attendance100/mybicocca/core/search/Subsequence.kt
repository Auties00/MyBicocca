package it.attendance100.mybicocca.core.search

/**
 * Result of [subsequenceMatch].
 *
 * @property normalizedScore Raw DP score over the best score this query could possibly
 *   reach, in 0..1.
 * @property positions Indices into the text of every matched character, ascending. They map
 *   1:1 onto the original string for highlighting.
 */
class SubsequenceMatch(
    val normalizedScore: Double,
    val positions: List<Int>,
)

/**
 * fzf-style in-order subsequence matcher with affine gaps, for abbreviation queries no
 * other tier catches: "anmat" -> "ANalisi MATematica", "fisgen" -> "FISica GENerale".
 *
 * Both strings must be pre-folded (lowercase, diacritics stripped) and the text must keep
 * its original indexing — the returned positions map 1:1 onto it for highlighting.
 *
 * Algorithm: dynamic programming over the query (length n) and the text (length m), O(n·m)
 * time and space, preceded by an O(m) plain-subsequence reject.
 * - `endsAt[i][j]` is the best score aligning `query[0..i]` with `text[0..j]` where
 *   `query[i] == text[j]`; `best[i][j]` is the best score of `query[0..i]` ending anywhere
 *   at or before `j`, maintained as a rolling row maximum with the affine inner-gap
 *   penalty (start -3, extension -1) folded in.
 * - A cell is reached either as a fresh match after a gap (from `best[i-1][j-1]`) or as a
 *   consecutive run (from `endsAt[i-1][j-1]`); a run keeps the larger of the positional
 *   bonus and the run bonus, mirroring fzf's carry-over rule. A source matrix records
 *   which path won, for the backtrack.
 * - Leading and trailing gaps are free, like fzf: the first query character may start
 *   anywhere and the terminal cell is the maximum over the last query row. The backtrack
 *   walks the recorded sources to recover the matched positions; after a gap it rewinds to
 *   the column where the previous row's `best` peaked.
 *
 * Scoring constants follow fzf's battle-tested values (match 16, boundary 8-10,
 * letter-digit transition 7, consecutive 4, gap -3/-1), with the first query character's
 * boundary bonus doubled.
 *
 * Two acceptance filters keep abbreviation noise out: the match must anchor on a word
 * start (a floating mid-word subsequence is meaningless), and the normalized score must
 * reach 0.5. Queries shorter than four characters never match.
 */
fun subsequenceMatch(query: String, text: String): SubsequenceMatch? {
    val n = query.length
    val m = text.length
    if (n < MinQueryLength || m < n) return null

    var qi = 0
    for (c in text) {
        if (qi < n && c == query[qi]) qi++
    }
    if (qi < n) return null

    val bonus = IntArray(m) { positionBonus(text, it) }

    val endsAt = Array(n) { IntArray(m) { NoScore } }
    val best = Array(n) { IntArray(m) { NoScore } }
    val from = Array(n) { ByteArray(m) }

    for (i in 0 until n) {
        var rolling = NoScore
        var rollingGapped = false
        for (j in 0 until m) {
            if (query[i] == text[j]) {
                val boundaryBonus = bonus[j] * (if (i == 0) FirstCharBonusMultiplier else 1)
                var score = NoScore
                var source: Byte = 0
                if (i == 0) {
                    score = ScoreMatch + boundaryBonus
                    source = 1
                } else if (j > 0) {
                    val afterGap = best[i - 1][j - 1]
                    if (afterGap != NoScore) {
                        score = afterGap + ScoreMatch + boundaryBonus
                        source = 1
                    }
                    val consecutive = endsAt[i - 1][j - 1]
                    if (consecutive != NoScore) {
                        val runScore = consecutive + ScoreMatch + maxOf(BonusConsecutive, bonus[j])
                        if (runScore > score) {
                            score = runScore
                            source = 2
                        }
                    }
                }
                if (score != NoScore) {
                    endsAt[i][j] = score
                    from[i][j] = source
                }
            }
            val gapped = if (rolling == NoScore) {
                NoScore
            } else {
                rolling + if (rollingGapped) ScoreGapExtension else ScoreGapStart
            }
            val here = endsAt[i][j]
            if (here != NoScore && (gapped == NoScore || here >= gapped)) {
                rolling = here
                rollingGapped = false
            } else {
                rolling = gapped
                rollingGapped = true
            }
            best[i][j] = rolling
        }
    }

    var endColumn = -1
    var endScore = NoScore
    for (j in 0 until m) {
        val score = endsAt[n - 1][j]
        if (score > endScore) {
            endScore = score
            endColumn = j
        }
    }
    if (endColumn < 0) return null

    val positions = IntArray(n)
    var i = n - 1
    var j = endColumn
    while (i >= 0) {
        positions[i] = j
        val source = from[i][j]
        i--
        if (i < 0) break
        j--
        if (source == 1.toByte()) {
            while (j > 0 && (endsAt[i][j] == NoScore || endsAt[i][j] != best[i][j])) j--
        }
    }

    if (bonus[positions[0]] < BonusBoundary) return null

    val perfect = n * (ScoreMatch + BonusConsecutive) +
        BonusBoundaryWhite * FirstCharBonusMultiplier - BonusConsecutive
    val normalized = (endScore.toDouble() / perfect).coerceIn(0.0, 1.0)
    if (normalized < MinNormalizedScore) return null
    return SubsequenceMatch(normalized, positions.toList())
}

/**
 * fzf-style positional bonus of the character at [index]: highest at the start of the text
 * or after whitespace, then after a delimiter, then after any other non-alphanumeric, then
 * on a letter-digit transition; zero mid-word. Non-alphanumeric characters themselves earn
 * no bonus.
 */
private fun positionBonus(text: String, index: Int): Int {
    val c = text[index]
    if (!c.isLetterOrDigit()) return 0
    if (index == 0) return BonusBoundaryWhite
    val previous = text[index - 1]
    return when {
        previous.isWhitespace() -> BonusBoundaryWhite
        previous == '/' || previous == '-' || previous == '_' ||
            previous == '.' || previous == ':' || previous == ',' -> BonusBoundaryDelimiter

        !previous.isLetterOrDigit() -> BonusBoundary
        c.isDigit() && previous.isLetter() -> BonusCamel
        c.isLetter() && previous.isDigit() -> BonusCamel
        else -> 0
    }
}

private const val ScoreMatch = 16
private const val ScoreGapStart = -3
private const val ScoreGapExtension = -1
private const val BonusBoundaryWhite = 10
private const val BonusBoundaryDelimiter = 9
private const val BonusBoundary = 8
private const val BonusCamel = 7
private const val BonusConsecutive = 4
private const val FirstCharBonusMultiplier = 2
private const val NoScore = Int.MIN_VALUE / 2
private const val MinQueryLength = 4
private const val MinNormalizedScore = 0.5
