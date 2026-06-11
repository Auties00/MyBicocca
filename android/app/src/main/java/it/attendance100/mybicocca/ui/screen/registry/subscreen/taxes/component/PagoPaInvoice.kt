package it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.component

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.component.shape.DynamicCard
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.theme.PagoPaColor
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.theme.PagoPaSecondaryColor
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.theme.pagoPaBackgroundColor

/**
 * pagoPA "payment successful" receipt shown on the detail of a paid invoice: the same sliced
 * ticket card as the fattura, washed in the pagoPA palette, with the pagoPA logo over the
 * transaction-success message.
 */
@Composable
fun PagoPaInvoice(modifier: Modifier = Modifier) {
    DynamicCard(
        topSliceRes = R.drawable.border_comp_outer,
        midSliceRes = R.drawable.body_outer,
        bottomSliceRes = R.drawable.border_outer,
        topSliceRes2 = R.drawable.border_comp_inner,
        midSliceRes2 = R.drawable.body_inner,
        bottomSliceRes2 = R.drawable.border_inner,
        sliceTopHeightDp = 10.dp,
        sliceBottomHeightDp = 10.dp,
        fill = pagoPaBackgroundColor(),
        stroke = PagoPaColor,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Icon(
                painter = painterResource(id = R.drawable.pagopa_white),
                modifier = Modifier.size(90.dp),
                contentDescription = stringResource(R.string.taxes_pagopa),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.taxes_transaction_outcome),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = PagoPaSecondaryColor,
            )
            Text(text = stringResource(R.string.taxes_payment_success))
            Spacer(Modifier.height(24.dp))
        }
    }
}
