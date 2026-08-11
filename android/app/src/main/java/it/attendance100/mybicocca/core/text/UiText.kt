package it.attendance100.mybicocca.core.text

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * Encapsulates UI text that can originate from a raw string or an Android string resource ID,
 * allowing non-UI layers (ViewModels, Repositories, Mappers) to defer string resolution
 * without holding an Android [Context].
 */
sealed interface UiText {
    data class DynamicString(val value: String) : UiText

    data class StringResource(
        @StringRes val resId: Int,
        val args: List<Any> = emptyList(),
    ) : UiText {
        constructor(@StringRes resId: Int, vararg args: Any) : this(resId, args.toList())
    }

    data class Composite(val items: List<UiText>, val separator: String = "") : UiText

    fun asString(context: Context): String = when (this) {
        is DynamicString -> value
        is StringResource -> if (args.isEmpty()) {
            context.getString(resId)
        } else {
            context.getString(resId, *args.toTypedArray())
        }

        is Composite -> items.joinToString(separator) { it.asString(context) }
    }

    @Composable
    fun asString(): String = when (this) {
        is DynamicString -> value
        is StringResource -> if (args.isEmpty()) {
            stringResource(resId)
        } else {
            stringResource(resId, *args.toTypedArray())
        }

        is Composite -> {
            val result = StringBuilder()
            for (i in items.indices) {
                if (i > 0) result.append(separator)
                result.append(items[i].asString())
            }
            result.toString()
        }
    }
}
