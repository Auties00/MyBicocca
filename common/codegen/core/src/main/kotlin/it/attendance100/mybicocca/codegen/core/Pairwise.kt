package it.attendance100.mybicocca.codegen.core

import kotlin.random.Random

/**
 * Picks combinations of values that try every pair of values of two dimensions together at least
 * once, with far fewer rows than every combination: a pairwise covering array, built greedily.
 */
object Pairwise {
    private const val CANDIDATES = 40

    /**
     * Rows of value indices, one per dimension of [sizes]: the first row is all zeros, and every
     * pair of values is in some row, unless [limit] rows are reached first. With one dimension,
     * the rows are its first values. The rows are the same for the same arguments.
     */
    fun rows(sizes: List<Int>, limit: Int, seed: Long): List<List<Int>> {
        require(sizes.all { it > 0 }) { "Every dimension needs a value" }
        require(limit > 0) { "The limit must be positive" }
        if (sizes.isEmpty()) return listOf(emptyList())
        val total = sizes.fold(1L) { product, size -> (product * size).coerceAtMost(Long.MAX_VALUE / 64) }
        if (total <= limit) return product(sizes)
        if (sizes.size == 1) return (0 until limit).map { listOf(it) }

        val uncovered = mutableSetOf<Pair>()
        for (i in sizes.indices) {
            for (j in i + 1 until sizes.size) {
                for (a in 0 until sizes[i]) for (b in 0 until sizes[j]) uncovered += Pair(i, a, j, b)
            }
        }

        val random = Random(seed)
        val rows = mutableListOf(List(sizes.size) { 0 })
        uncovered -= pairs(rows.first())
        while (uncovered.isNotEmpty() && rows.size < limit) {
            val best = (0 until CANDIDATES)
                .map { candidate(sizes, uncovered, random) }
                .maxBy { row -> pairs(row).count { it in uncovered } }
            rows += best
            uncovered -= pairs(best)
        }
        return rows
    }

    private data class Pair(val i: Int, val a: Int, val j: Int, val b: Int)

    private fun pairs(row: List<Int>): List<Pair> = buildList {
        for (i in row.indices) for (j in i + 1 until row.size) add(Pair(i, row[i], j, row[j]))
    }

    /** A row starting from a random uncovered pair, filled dimension by dimension with the value covering most new pairs. */
    private fun candidate(sizes: List<Int>, uncovered: Set<Pair>, random: Random): List<Int> {
        val start = uncovered.elementAt(random.nextInt(uncovered.size))
        val row = IntArray(sizes.size) { -1 }
        row[start.i] = start.a
        row[start.j] = start.b
        for (dimension in sizes.indices.shuffled(random)) {
            if (row[dimension] >= 0) continue
            row[dimension] = (0 until sizes[dimension]).shuffled(random).maxBy { value ->
                sizes.indices.count { other ->
                    other != dimension && row[other] >= 0 &&
                        (if (other < dimension) Pair(other, row[other], dimension, value) else Pair(dimension, value, other, row[other])) in uncovered
                }
            }
        }
        return row.toList()
    }

    /** Every combination, in order, the first being all zeros. */
    private fun product(sizes: List<Int>): List<List<Int>> =
        sizes.fold(listOf(emptyList())) { rows, size -> rows.flatMap { row -> (0 until size).map { row + it } } }
}
