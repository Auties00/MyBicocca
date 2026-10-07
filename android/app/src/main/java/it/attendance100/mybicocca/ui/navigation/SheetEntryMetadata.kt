package it.attendance100.mybicocca.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavEntry
import it.attendance100.mybicocca.ui.component.modal.SheetContainerStyle
import it.attendance100.mybicocca.ui.component.modal.SheetHeaderSpec
import it.attendance100.mybicocca.ui.component.modal.SheetPager
import it.attendance100.mybicocca.ui.navigation.route.Route

/*
 * Metadata a sheet entry declares for the modal scene: its pinned header and, on a sheet's first
 * page, the sheet's container look. Combine with `+`, e.g. `sheetHeaderInPage() + sheetStyle(…)`.
 */

/**
 * Sheet-page metadata payload: the page's pinned header resolver, or null when the page draws
 * its header itself through a nested [SheetPager] ([sheetHeaderInPage]).
 */
internal class SheetPageOptions(val header: (@Composable (Route) -> SheetHeaderSpec)?)

private const val SheetPageMetadataKey: String = "it.attendance100.mybicocca.sheetPage"
private const val SheetStyleMetadataKey: String = "it.attendance100.mybicocca.sheetStyle"

/**
 * Metadata giving a sheet entry its pinned [SheetHeaderSpec] header (title and subtitle), e.g.
 * `entry<SheetRoute.IseeDetail>(metadata = sheetHeader<SheetRoute.IseeDetail> { key -> … })`.
 * Required on every sheet route: a page without it fails fast when shown. The resolver gets the
 * entry's own route, so a page can render its header even while it is the outgoing side of a
 * transition.
 */
inline fun <reified R : Route> sheetHeader(
    noinline header: @Composable (R) -> SheetHeaderSpec,
): Map<String, Any> = sheetHeaderMetadata { route -> header(route as R) }

/** A pinned header with a fixed title and subtitle. */
fun staticSheetHeader(@StringRes title: Int, @StringRes subtitle: Int): Map<String, Any> =
    sheetHeaderMetadata { SheetHeaderSpec(title = stringResource(title), subtitle = stringResource(subtitle)) }

/**
 * Metadata for a sheet page that has its own page machine on a nested [SheetPager] (a wizard, a
 * list -> detail -> result flow): that pager draws the title and subtitle of each of its pages,
 * so the sheet adds no header of its own above it.
 */
fun sheetHeaderInPage(): Map<String, Any> = sheetHeaderMetadata(null)

@PublishedApi
internal fun sheetHeaderMetadata(header: (@Composable (Route) -> SheetHeaderSpec)?): Map<String, Any> =
    mapOf(SheetPageMetadataKey to SheetPageOptions(header))

/** Metadata giving a sheet (declared on its first page) a [SheetContainerStyle]. */
fun sheetStyle(style: SheetContainerStyle): Map<String, Any> = mapOf(SheetStyleMetadataKey to style)

internal val NavEntry<*>.sheetPageOptions: SheetPageOptions?
    get() = metadata[SheetPageMetadataKey] as? SheetPageOptions

internal val NavEntry<*>.sheetContainerStyle: SheetContainerStyle?
    get() = metadata[SheetStyleMetadataKey] as? SheetContainerStyle
