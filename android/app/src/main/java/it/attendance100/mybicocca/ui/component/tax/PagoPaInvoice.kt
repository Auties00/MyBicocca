package it.attendance100.mybicocca.ui.component.tax

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.component.shape.DynamicCard
import it.attendance100.mybicocca.ui.theme.PagoPaColor
import it.attendance100.mybicocca.ui.theme.PagoPaSecondaryColor
import it.attendance100.mybicocca.ui.theme.pagoPaBackgroundColor

@Composable
fun PagoPaInvoice(
    modifier: Modifier = Modifier,
) {
    val pagoPaBackgroundColor = pagoPaBackgroundColor()

    DynamicCard(
        R.drawable.border_comp_outer,
        R.drawable.body_outer,
        R.drawable.border_outer,
        R.drawable.border_comp_inner,
        R.drawable.body_inner,
        R.drawable.border_inner,
        fill = pagoPaBackgroundColor,
        stroke = PagoPaColor,
        sliceTopHeightDp = 10.dp,
        sliceBottomHeightDp = 10.dp,
        modifier = modifier,
        onClick = {}
    ) {
        // Actual Content Overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Icon(
                painter = painterResource(id = R.drawable.pagopa_white),
                modifier = Modifier.size(90.dp),
                contentDescription = "PagoPA",
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Esito Transazione",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = PagoPaSecondaryColor,
            )
            Text(
                text = "Il pagamento si \u00E8 completato con successo per l'intera somma dovuta",
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
