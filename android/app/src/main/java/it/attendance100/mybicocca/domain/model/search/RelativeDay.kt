package it.attendance100.mybicocca.domain.model.search

/**
 * A relative-day search hit — "today", "tomorrow", or "the day after tomorrow" — recognized
 * from a date query. Carries no display text: the wording is a localized string resource
 * resolved at the UI layer, keeping this domain type Android-free.
 */
enum class RelativeDay {
    Today,
    Tomorrow,
    AfterTomorrow;

    companion object {
        /**
         * Maps the raw offset produced by the date parser (0 = today, 1 = tomorrow,
         * 2 = the day after) to a [RelativeDay], returning null for any other value.
         */
        fun fromOffset(offset: Int): RelativeDay? = when (offset) {
            0 -> Today
            1 -> Tomorrow
            2 -> AfterTomorrow
            else -> null
        }
    }
}
