package it.attendance100.mybicocca.core.state

/**
 * Wraps data sourced from a local cache so the UI can tell "the cache has not emitted yet" apart
 * from "the cache is empty".
 *
 * ViewModels expose `StateFlow<Loadable<T>>` seeded with [NotYetLoaded]; the first Room (or
 * DataStore) emission switches it to [Loaded]. Screens render a loading placeholder for
 * [NotYetLoaded] and an empty state only for a [Loaded] empty value, which keeps empty-state
 * flashes off the first frame.
 */
sealed interface Loadable<out T> {
    /** The backing store has not produced a value yet: show a loading placeholder, never an empty state. */
    data object NotYetLoaded : Loadable<Nothing>

    /** The backing store has emitted; [value] may still be empty, which is a genuine empty state. */
    data class Loaded<T>(val value: T) : Loadable<T>
}

/** Transforms the loaded value while passing [Loadable.NotYetLoaded] through unchanged. */
inline fun <T, R> Loadable<T>.map(transform: (T) -> R): Loadable<R> = when (this) {
    Loadable.NotYetLoaded -> Loadable.NotYetLoaded
    is Loadable.Loaded -> Loadable.Loaded(transform(value))
}

/** The loaded value, or null while [Loadable.NotYetLoaded]. */
fun <T> Loadable<T>.valueOrNull(): T? = (this as? Loadable.Loaded)?.value
