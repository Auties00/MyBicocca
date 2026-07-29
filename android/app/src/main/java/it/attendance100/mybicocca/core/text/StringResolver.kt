package it.attendance100.mybicocca.core.text

import android.content.Context
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Abstraction for string resource resolution, allowing use cases and domain components to
 * score or resolve localized strings without depending directly on Android [Context] or
 * Hilt `@ApplicationContext`.
 */
interface StringResolver {
    fun getString(@StringRes resId: Int): String
    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String

    /**
     * The [Locale] strings are actually resolved in. Callers that cache resolved strings should
     * key on this — not [Locale.getDefault] — so the cache tracks the same configuration the
     * resolution reads from and stays consistent across per-app language changes.
     */
    fun currentLocale(): Locale
}

@Singleton
class ContextStringResolver @Inject constructor(
    @ApplicationContext private val context: Context
) : StringResolver {
    override fun getString(resId: Int): String = context.getString(resId)
    override fun getString(resId: Int, vararg formatArgs: Any): String =
        context.getString(resId, *formatArgs)

    override fun currentLocale(): Locale =
        context.resources.configuration.locales.get(0) ?: Locale.getDefault()
}
