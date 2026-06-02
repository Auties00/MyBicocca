package it.attendance100.mybicocca.ui.screen.profile.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.ui.theme.BadgeWhiteDrawableColor
import it.attendance100.mybicocca.ui.theme.OnBackgroundColor
import java.io.File

// The interactive student ID badge (flippable [CreditCard] bound to the front/back faces).
// Extracted so it can be hosted either inline on the profile screen or as a shell-level
// overlay that floats above the top bar.
@Composable
fun StudentCard(
    account: Account?,
    career: Career?,
    photoFile: File?,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true,
) {
    CreditCard(
        modifier = modifier,
        accentColor = accent,
        isChromatic = true,
        enabled = enabled,
        frontContent = { x, y, white, _ ->
            BadgeFront(
                account = account,
                career = career,
                textColor = if (white) BadgeWhiteDrawableColor else OnBackgroundColor,
                touchX = x,
                touchY = y,
                whiteBadge = white,
            )
        },
        backContent = { x, y, white, haze ->
            BadgeBack(
                account = account,
                photoFile = photoFile,
                textColor = if (white) BadgeWhiteDrawableColor else OnBackgroundColor,
                touchX = x,
                touchY = y,
                whiteBadge = white,
                hazeState = haze,
            )
        },
    )
}
