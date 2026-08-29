package com.marlena.martins.sellcieapplication.presentation.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.marlena.martins.sellcieapplication.R
import com.marlena.martins.sellcieapplication.data.payment.CieloConstants
import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.domain.usecase.PurchaseReceipt
import com.marlena.martins.sellcieapplication.presentation.catalog.CatalogTestTags
import com.marlena.martins.sellcieapplication.presentation.catalog.components.CieloAppHeader
import com.marlena.martins.sellcieapplication.presentation.catalog.formatCurrency

@Composable
fun ReceiptScreen(
    receipt: PurchaseReceipt?,
    outcome: PaymentOutcome?,
    onViewTickets: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { 
            CieloAppHeader(
                title = stringResource(R.string.checkout_title),
                subtitle = stringResource(R.string.receipt_subtitle)
            ) 
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.receipt_headline),
                style = MaterialTheme.typography.headlineMedium
            )
            if (receipt == null || outcome == null) {
                Text(text = stringResource(R.string.receipt_error_fetch))
            } else {
                val (titleRes, messageRes) = outcome.presentation()
                Card(modifier = Modifier.fillMaxWidth().semantics { testTag = CatalogTestTags.RECEIPT_CARD }) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(titleRes),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(messageRes),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.receipt_items_title),
                            fontWeight = FontWeight.SemiBold
                        )
                        receipt.items.forEach { item ->
                            Text(item.title, fontWeight = FontWeight.Medium)
                            Text(
                                text = stringResource(
                                    R.string.receipt_item_detail,
                                    item.quantity,
                                    formatCurrency(item.unitPriceInCents),
                                    formatCurrency(item.subtotalInCents)
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(text = stringResource(R.string.receipt_total_quantity, receipt.totalQuantity))
                        Text(text = stringResource(R.string.receipt_total_label, formatCurrency(receipt.totalInCents)))
                        Text(text = stringResource(R.string.receipt_date_label, receipt.formattedDate))
                        Text(text = stringResource(R.string.receipt_reference_label, receipt.shortReference))

                        receipt.cieloMetadata?.let { meta ->
                            Text(
                                text = stringResource(R.string.receipt_cielo_data_title),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            meta[CieloConstants.KEY_BRAND]?.let { Text(stringResource(R.string.receipt_cielo_brand, it)) }
                            meta[CieloConstants.KEY_AUTH_CODE]?.let { Text(stringResource(R.string.receipt_cielo_auth, it)) }
                            meta[CieloConstants.KEY_CIELO_CODE]?.let { Text(stringResource(R.string.receipt_cielo_nsu, it)) }
                            meta[CieloConstants.KEY_CARD_MASK]?.let { Text(stringResource(R.string.receipt_cielo_card, it)) }
                            meta[CieloConstants.KEY_TERMINAL]?.let { Text(stringResource(R.string.receipt_cielo_terminal, it)) }
                            meta[CieloConstants.KEY_REASON]?.let { Text(stringResource(R.string.receipt_cielo_reason, it), color = MaterialTheme.colorScheme.error) }
                        }
                    }
                }
            }
            if (outcome == PaymentOutcome.Approved && receipt != null) {
                Button(
                    onClick = onViewTickets,
                    modifier = Modifier.fillMaxWidth().semantics {
                        testTag = CatalogTestTags.RECEIPT_VIEW_TICKETS_BUTTON
                    }
                ) { Text(stringResource(R.string.receipt_view_tickets)) }
            }
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().semantics { testTag = CatalogTestTags.RECEIPT_BACK_BUTTON }
            ) { Text(stringResource(R.string.checkout_back_button)) }
        }
    }
}

private fun PaymentOutcome.presentation(): Pair<Int, Int> = when (this) {
    PaymentOutcome.Approved -> R.string.payment_outcome_approved_title to R.string.payment_outcome_approved_msg
    PaymentOutcome.Declined -> R.string.payment_outcome_declined_title to R.string.payment_outcome_declined_msg
    PaymentOutcome.Canceled -> R.string.payment_outcome_canceled_title to R.string.payment_outcome_canceled_msg
    PaymentOutcome.TechnicalError -> R.string.payment_outcome_technical_error_title to R.string.payment_outcome_technical_error_msg
    PaymentOutcome.Pending -> R.string.payment_outcome_pending_title to R.string.payment_outcome_pending_msg
}
