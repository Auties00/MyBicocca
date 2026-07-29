package it.attendance100.mybicocca.core.text

import java.util.Locale

/**
 * Thread-safe helper that caches a transformed catalog [R] based on the current system [Locale].
 */
class LocaleCachedCatalog<T, R>(
    private val items: List<T>,
    private val transform: (T, StringResolver) -> R,
) {
    @Volatile
    private var cachedLocale: Locale? = null

    @Volatile
    private var cachedEntries: List<R> = emptyList()

    @Synchronized
    fun get(stringResolver: StringResolver): List<R> {
        val currentLocale = stringResolver.currentLocale()
        if (cachedLocale == currentLocale && cachedEntries.isNotEmpty()) {
            return cachedEntries
        }
        val resolved = items.map { transform(it, stringResolver) }
        cachedLocale = currentLocale
        cachedEntries = resolved
        return resolved
    }
}
