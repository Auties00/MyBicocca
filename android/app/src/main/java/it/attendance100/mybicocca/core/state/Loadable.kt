package it.attendance100.mybicocca.core.state

sealed interface Loadable<out T> {
    data object NotYetLoaded : Loadable<Nothing>
    data class Loaded<T>(val value: T) : Loadable<T>
}

inline fun <T, R> Loadable<T>.map(transform: (T) -> R): Loadable<R> = when (this) {
    Loadable.NotYetLoaded -> Loadable.NotYetLoaded
    is Loadable.Loaded -> Loadable.Loaded(transform(value))
}

fun <T> Loadable<T>.valueOrNull(): T? = (this as? Loadable.Loaded)?.value
