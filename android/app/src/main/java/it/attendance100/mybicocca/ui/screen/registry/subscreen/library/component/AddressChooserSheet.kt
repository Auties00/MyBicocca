package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.component.button.ConnectedButtonPair
import it.attendance100.mybicocca.ui.component.button.ConnectedButtonPairDefaults
import it.attendance100.mybicocca.ui.component.button.PairButtonLabel
import it.attendance100.mybicocca.ui.component.modal.ModalSheetController
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import it.attendance100.mybicocca.ui.component.modal.SheetHeaderSpec
import it.attendance100.mybicocca.ui.component.modal.SheetPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreview
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryTestTags
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.openGeoAddressSafely
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.resolveDefaultMapsAppLabel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.LibraryAddressChooserState
import it.attendance100.mybicocca.ui.theme.PreviewBgDark
import kotlinx.coroutines.launch

/**
 * Second modal bottom sheet opened above the library sheet from the address row: shows the cleaned
 * address card and a [ConnectedButtonPair] with "Apri in {app}" leading and "Mostra sulla mappa"
 * trailing (or a lone primary-colored "Apri in {app}" button without a map building code).
 */
@Composable
internal fun AddressChooserSheet(
    state: LibraryAddressChooserState,
    onShowOnMap: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val controller = remember { ModalSheetController() }
    val scope = rememberCoroutineScope()
    var closing by remember { mutableStateOf(false) }

    PredictiveModalBottomSheet(
        onDismiss = onDismiss,
        controller = controller,
    ) {
        SheetPage(
            header = SheetHeaderSpec(
                title = stringResource(R.string.library_address_title),
                subtitle = null,
            ),
        ) {
            AddressChooserContent(
                state = state,
                onShowOnMap = { buildingCode ->
                    if (!closing) {
                        closing = true
                        scope.launch {
                            controller.hide()
                            onShowOnMap(buildingCode)
                        }
                    }
                },
                onOpenExternal = {
                    if (!closing) {
                        closing = true
                        openGeoAddressSafely(context, state.address)
                        onDismiss()
                    }
                },
            )
        }
    }
}

@Composable
private fun AddressChooserContent(
    state: LibraryAddressChooserState,
    onShowOnMap: (String) -> Unit,
    onOpenExternal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val inspection = LocalInspectionMode.current
    val scheme = MaterialTheme.colorScheme

    val mapsAppLabel = remember(context, inspection) {
        if (inspection) null else resolveDefaultMapsAppLabel(context)
    }
    val externalLabel = if (mapsAppLabel != null) {
        stringResource(R.string.library_open_in_app, mapsAppLabel)
    } else {
        stringResource(R.string.library_open_in_maps)
    }
    val showOnMapLabel = stringResource(R.string.library_show_on_map)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
            .testTag(LibraryTestTags.ADDRESS_CHOOSER_PAGE),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = scheme.surfaceContainerHigh,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    text = state.address,
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        val buildingCode = state.buildingCode
        if (buildingCode != null) {
            ConnectedButtonPair(
                secondary = {
                    Row(
                        modifier = Modifier.testTag(LibraryTestTags.ADDRESS_CHOOSER_EXTERNAL),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PairButtonLabel(externalLabel)
                    }
                },
                onSecondary = onOpenExternal,
                primary = {
                    Row(
                        modifier = Modifier.testTag(LibraryTestTags.ADDRESS_CHOOSER_MAP),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PairButtonLabel(showOnMapLabel)
                    }
                },
                onPrimary = { onShowOnMap(buildingCode) },
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        } else {
            ConnectedButtonPair(
                secondary = {
                    Row(
                        modifier = Modifier.testTag(LibraryTestTags.ADDRESS_CHOOSER_EXTERNAL),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PairButtonLabel(externalLabel)
                    }
                },
                onSecondary = onOpenExternal,
                secondaryColors = ConnectedButtonPairDefaults.primaryColors(),
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        }
    }
}

@Preview(name = "AddressChooser With Map · Light", showBackground = true)
@Composable
private fun AddressChooserWithMapLightPreview() {
    LibraryPreview(dark = false) {
        AddressChooserContent(
            state = LibraryAddressChooserState(
                address = "Piazza dell'Ateneo Nuovo 1, 20126 Milano MI",
                buildingCode = "U06",
            ),
            onShowOnMap = {},
            onOpenExternal = {},
        )
    }
}

@Preview(
    name = "AddressChooser With Map · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun AddressChooserWithMapDarkPreview() {
    LibraryPreview(dark = true) {
        AddressChooserContent(
            state = LibraryAddressChooserState(
                address = "Via Cadore 48, 20900 Monza MB",
                buildingCode = "U08",
            ),
            onShowOnMap = {},
            onOpenExternal = {},
        )
    }
}

@Preview(name = "AddressChooser No Building · Light", showBackground = true)
@Composable
private fun AddressChooserNoBuildingLightPreview() {
    LibraryPreview(dark = false) {
        AddressChooserContent(
            state = LibraryAddressChooserState(
                address = "Piazza dell'Ateneo Nuovo 1, 20126 Milano MI",
                buildingCode = null,
            ),
            onShowOnMap = {},
            onOpenExternal = {},
        )
    }
}

@Preview(
    name = "AddressChooser No Building · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun AddressChooserNoBuildingDarkPreview() {
    LibraryPreview(dark = true) {
        AddressChooserContent(
            state = LibraryAddressChooserState(
                address = "Piazza dell'Ateneo Nuovo 1, 20126 Milano MI",
                buildingCode = null,
            ),
            onShowOnMap = {},
            onOpenExternal = {},
        )
    }
}
