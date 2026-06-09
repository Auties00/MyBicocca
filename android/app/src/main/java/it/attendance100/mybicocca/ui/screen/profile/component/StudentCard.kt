package it.attendance100.mybicocca.ui.screen.profile.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.ui.theme.BadgeCardTheme
import it.attendance100.mybicocca.ui.theme.colors
import java.io.File

// The interactive student ID badge (flippable [CreditCard] bound to the front/back faces).
// Extracted so it can be hosted either inline on the profile screen or as a shell-level
// overlay that floats above the top bar. The visual style is driven by [theme]; its palette
// is resolved once here and threaded through the card so the faces never branch on the theme.
@Composable
fun StudentCard(
    account: Account?,
    career: Career?,
    photoFile: File?,
    modifier: Modifier = Modifier,
    theme: BadgeCardTheme = BadgeCardTheme.Default,
    accent: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true,
) {
    CreditCard(
        modifier = modifier,
        colors = theme.colors(),
        accentColor = accent,
        isChromatic = true,
        enabled = enabled,
        frontContent = { x, y, colors, _ ->
            BadgeFront(
                account = account,
                career = career,
                colors = colors,
                touchX = x,
                touchY = y,
            )
        },
        backContent = { x, y, colors, haze ->
            BadgeBack(
                account = account,
                photoFile = photoFile,
                colors = colors,
                touchX = x,
                touchY = y,
                hazeState = haze,
            )
        },
    )
}
