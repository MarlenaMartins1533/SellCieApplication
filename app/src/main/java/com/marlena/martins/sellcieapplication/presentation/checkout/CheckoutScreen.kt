package com.marlena.martins.sellcieapplication.presentation.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.marlena.martins.sellcieapplication.R
import com.marlena.martins.sellcieapplication.presentation.catalog.CatalogTestTags
import com.marlena.martins.sellcieapplication.presentation.catalog.PaymentUiState
import com.marlena.martins.sellcieapplication.presentation.catalog.components.CieloAppHeader
import com.marlena.martins.sellcieapplication.presentation.catalog.formatCurrency

@Composable
fun CheckoutScreen(
    selectedTicketCount: Int,
    totalInCents: Long,
    paymentState: PaymentUiState,
    onConfirm: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            CieloAppHeader(
                title = stringResource(R.string.checkout_title),
                subtitle = stringResource(R.string.checkout_subtitle)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.checkout_confirm_headline),
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = stringResource(R.string.checkout_confirm_description),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.checkout_order_summary),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = stringResource(R.string.checkout_tickets_label, selectedTicketCount))
                    Text(
                        text = formatCurrency(totalInCents),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            when (paymentState) {
                PaymentUiState.Idle -> {
                    Text(
                        text = stringResource(R.string.checkout_local_notice),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { testTag = CatalogTestTags.CHECKOUT_CONFIRM_BUTTON }
                    ) { Text(stringResource(R.string.checkout_confirm_button)) }
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { testTag = CatalogTestTags.CHECKOUT_BACK_BUTTON }
                    ) { Text(stringResource(R.string.checkout_back_button)) }
                }

                is PaymentUiState.Processing -> ProcessingContent()
                is PaymentUiState.Result -> Unit
            }

            if (paymentState is PaymentUiState.Result) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = CatalogTestTags.CHECKOUT_BACK_BUTTON }
                ) {
                    Text(stringResource(R.string.checkout_back_button))
                }
            }
        }
    }
}

@Composable
private fun ProcessingContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator()
        Text(stringResource(R.string.checkout_processing_title))
        Text(
            text = stringResource(R.string.checkout_processing_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
