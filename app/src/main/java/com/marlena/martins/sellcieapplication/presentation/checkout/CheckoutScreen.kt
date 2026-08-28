package com.marlena.martins.sellcieapplication.presentation.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.presentation.catalog.CatalogTestTags
import com.marlena.martins.sellcieapplication.presentation.catalog.PaymentUiState
import com.marlena.martins.sellcieapplication.presentation.catalog.components.CieloAppHeader
import com.marlena.martins.sellcieapplication.presentation.catalog.formatCurrency

@Composable
fun CheckoutScreen(
    selectedTicketCount: Int,
    totalInCents: Long,
    paymentState: PaymentUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            CieloAppHeader(
                title = "Cielo Ingressos",
                subtitle = "Pagamento local seguro"
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
            Text("Processando pagamento", style = MaterialTheme.typography.headlineMedium)
            Text(
                "A confirmação é simulada localmente para este estudo offline.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Resumo do pedido", fontWeight = FontWeight.SemiBold)
                    Text("$selectedTicketCount ingresso(s)")
                    Text(
                        formatCurrency(totalInCents),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            when (paymentState) {
                PaymentUiState.Idle,
                is PaymentUiState.Processing -> ProcessingContent()
                is PaymentUiState.Result -> PaymentResultContent(paymentState.outcome)
            }

            if (paymentState is PaymentUiState.Result) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = CatalogTestTags.CHECKOUT_BACK_BUTTON }
                ) {
                    Text("Voltar ao catálogo")
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
        Text("Processando pagamento…")
        Text(
            "Aguarde a confirmação local.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PaymentResultContent(outcome: PaymentOutcome) {
    val (title, message) = when (outcome) {
        PaymentOutcome.Approved -> "Pagamento aprovado" to "A confirmação local foi registrada."
        PaymentOutcome.Pending -> "Pagamento pendente" to "A confirmação local ainda está pendente."
        PaymentOutcome.Declined -> "Pagamento não aprovado" to "Verifique a forma de pagamento e tente novamente."
        PaymentOutcome.Canceled -> "Pagamento cancelado" to "Nenhuma nova cobrança será enviada para esta tentativa."
        PaymentOutcome.TechnicalError -> "Não foi possível concluir" to "Tente novamente ou volte ao catálogo."
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
