package it.attendance100.mybicocca.ui.navigation.transitions

/**
 * Identity of a shared element across two nav entries: both sides of a transition must pass equal
 * keys to the bicocca shared modifiers for the element to match and morph.
 */
sealed interface SharedElementKey {
    val id: String
}