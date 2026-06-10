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

/**
 * Interactive student ID badge: a flippable [CreditCard] in the chromatic finish, with
 * [BadgeFront] (logo art, chip, holder name, matricola) and [BadgeBack] (magnetic stripe,
 * signature box, photo, institutional email) bound to its faces.
 */
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
