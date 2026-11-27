package it.unimib.indoornav.lab.inventory

/**
 * Keeps the tiles whose label, code, place, MAC or IDs contain [query] (ignoring case), or match it as a
 * regular expression when [regex] is on. Returns null for an invalid expression, so the caller can say so.
 */
object BeaconFilter {
    fun apply(tiles: List<ResolvedBeaconTile>, query: String, regex: Boolean): List<ResolvedBeaconTile>? {
        val q = query.trim()
        if (q.isEmpty()) return tiles
        val pattern = if (regex) {
            runCatching { Regex(q, RegexOption.IGNORE_CASE) }.getOrNull() ?: return null
        } else {
            null
        }
        return tiles.filter { tile ->
            fieldsOf(tile).any { field ->
                if (pattern != null) pattern.containsMatchIn(field) else field.contains(q, ignoreCase = true)
            }
        }
    }

    private fun fieldsOf(tile: ResolvedBeaconTile): List<String> = listOf(
        tile.displayLabel,
        tile.alphanumericCode,
        tile.building,
        tile.floor,
        tile.positionDescription,
        tile.macAddress,
        tile.canonicalKey,
    ) + (tile.savedEntry?.linkedKeys ?: emptyList())
}
