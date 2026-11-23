package it.unimib.indoornav.lab.inventory

object NaturalOrderComparator : Comparator<String> {
    override fun compare(a: String?, b: String?): Int {
        if (a === b) return 0
        if (a == null) return -1
        if (b == null) return 1

        var i = 0
        var j = 0
        var firstCaseDiff = 0
        while (i < a.length && j < b.length) {
            val ca = a[i]
            val cb = b[j]
            val da = ca.isDigit()
            val db = cb.isDigit()

            if (da && db) {
                val startA = i
                val startB = j
                while (i < a.length && a[i].isDigit()) i++
                while (j < b.length && b[j].isDigit()) j++

                val cmp = compareNumericChunks(a, startA, i, b, startB, j)
                if (cmp != 0) return cmp
            } else if (da != db) {
                return if (da) -1 else 1
            } else {
                val startA = i
                val startB = j
                while (i < a.length && !a[i].isDigit()) i++
                while (j < b.length && !b[j].isDigit()) j++

                val subA = a.substring(startA, i)
                val subB = b.substring(startB, j)
                val ci = subA.compareTo(subB, ignoreCase = true)
                if (ci != 0) return ci
                if (firstCaseDiff == 0) {
                    firstCaseDiff = subA.compareTo(subB)
                }
            }
        }
        return if (a.length != b.length) a.length - b.length else firstCaseDiff
    }

    private fun compareNumericChunks(
        a: String,
        startA: Int,
        endA: Int,
        b: String,
        startB: Int,
        endB: Int,
    ): Int {
        var nzA = startA
        while (nzA < endA && a[nzA] == '0') nzA++
        var nzB = startB
        while (nzB < endB && b[nzB] == '0') nzB++

        val lenA = endA - nzA
        val lenB = endB - nzB
        if (lenA != lenB) return lenA - lenB

        for (k in 0 until lenA) {
            val d = a[nzA + k] - b[nzB + k]
            if (d != 0) return d
        }
        return (endA - startA) - (endB - startB)
    }
}
