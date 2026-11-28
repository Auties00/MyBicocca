package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.EventSeat
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LibraryAdd
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.ui.component.directory.segmentedShape
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreview
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryTestTags
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.openCustomTabSafely
import it.attendance100.mybicocca.ui.theme.PreviewBgDark

private const val SERVICES_BASE_URL = "https://www.biblio.unimib.it/it/servizi"

private data class LibraryServiceLink(
    val slug: String,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
)

private val ServiceLinks: List<LibraryServiceLink> = listOf(
    LibraryServiceLink("accesso-e-consultazione", R.string.library_service_access, Icons.Outlined.Badge),
    LibraryServiceLink("spazi-studio", R.string.library_service_study_spaces, Icons.Outlined.EventSeat),
    LibraryServiceLink("prestito-libri-e-ebook", R.string.library_service_loans, Icons.AutoMirrored.Outlined.MenuBook),
    LibraryServiceLink("fotocopiatura-e-stampa", R.string.library_service_print, Icons.Outlined.Print),
    LibraryServiceLink("formazione-degli-utenti", R.string.library_service_training, Icons.Outlined.School),
    LibraryServiceLink("servizi-interbibliotecari", R.string.library_service_interlibrary, Icons.Outlined.SwapHoriz),
    LibraryServiceLink("acquisizione-risorse-bibliografiche", R.string.library_service_acquisition, Icons.Outlined.LibraryAdd),
    LibraryServiceLink("utenti-esterni-e-alumni", R.string.library_service_external, Icons.Outlined.Groups),
    LibraryServiceLink("open-access-sconti-autori-it", R.string.library_service_open_access, Icons.Outlined.LockOpen),
)

/**
 * Library web services directory: connected rows opening each Bicocca library service page in a
 * Custom Tab, followed by a separate "Tutti i servizi" entry.
 */
@Composable
internal fun ServicesPage(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 760.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
            .testTag(LibraryTestTags.SERVICES_PAGE),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            ServiceLinks.forEachIndexed { index, item ->
                ServiceRow(
                    icon = item.icon,
                    label = stringResource(item.labelRes),
                    isFirst = index == 0,
                    isLast = index == ServiceLinks.lastIndex,
                    onClick = { openCustomTabSafely(context, "$SERVICES_BASE_URL/${item.slug}") },
                    modifier = Modifier.testTag(LibraryTestTags.service(item.slug)),
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        ServiceRow(
            icon = Icons.Outlined.Public,
            label = stringResource(R.string.library_service_all),
            isFirst = true,
            isLast = true,
            onClick = { openCustomTabSafely(context, SERVICES_BASE_URL) },
            modifier = Modifier.testTag(LibraryTestTags.SERVICES_ALL),
        )
    }
}

@Composable
private fun ServiceRow(
    icon: ImageVector,
    label: String,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()

    Surface(
        onClick = { haptic.tap(); onClick() },
        shape = segmentedShape(isFirst = isFirst, isLast = isLast),
        color = scheme.surfaceContainerHigh,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(scheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = scheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                contentDescription = null,
                tint = scheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Preview(name = "Services · Light", showBackground = true)
@Composable
private fun ServicesLightPreview() {
    LibraryPreview(dark = false) {
        ServicesPage()
    }
}

@Preview(
    name = "Services · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun ServicesDarkPreview() {
    LibraryPreview(dark = true) {
        ServicesPage()
    }
}
