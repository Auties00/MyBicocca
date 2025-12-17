package it.attendance100.mybicocca.ui.screen.main.career

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.CareerStats


@Composable
fun PianoTab(
    user: User?,
    stats: CareerStats?,
) {
    Text(
        text = stringResource(R.string.career_tab_piano),
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
    )
}
