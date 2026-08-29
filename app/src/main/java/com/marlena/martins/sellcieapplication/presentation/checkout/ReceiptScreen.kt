package com.marlena.martins.sellcieapplication.presentation.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    Scaffold(topBar = { CieloAppHeader("Cielo Ingressos", "Comprovante da compra") }) { padding ->
        Column(
            modifier = modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Comprovante", style = MaterialTheme.typography.headlineMedium)
            if (receipt == null || outcome == null) {
                Text("Não foi possível recuperar o comprovante local.")
            } else {
                val (title, message) = outcome.presentation()
                Card(modifier = Modifier.fillMaxWidth().semantics { testTag = CatalogTestTags.RECEIPT_CARD }) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Itens do pedido", fontWeight = FontWeight.SemiBold)
                        receipt.items.forEach { item ->
                            Text(item.title, fontWeight = FontWeight.Medium)
                            Text(
                                "${item.quantity} ingresso(s) × ${formatCurrency(item.unitPriceInCents)} = " +
                                    formatCurrency(item.subtotalInCents),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text("Quantidade total: ${receipt.totalQuantity}")
                        Text("Total: ${formatCurrency(receipt.totalInCents)}")
                        Text("Data: ${receipt.formattedDate}")
                        Text("Referência: ${receipt.shortReference}")

                        receipt.cieloMetadata?.let { meta ->
                            Text("Dados Cielo", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
                            meta["brand"]?.let { Text("Bandeira: $it") }
                            meta["authCode"]?.let { Text("Autorização: $it") }
                            meta["cieloCode"]?.let { Text("NSU: $it") }
                            meta["mask"]?.let { Text("Cartão: $it") }
                            meta["terminal"]?.let { Text("Terminal: $it") }
                            meta["reason"]?.let { Text("Motivo: $it", color = MaterialTheme.colorScheme.error) }
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
                ) { Text("Ver meus ingressos") }
            }
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().semantics { testTag = CatalogTestTags.RECEIPT_BACK_BUTTON }
            ) { Text("Voltar ao catálogo") }
        }
    }
}

private fun PaymentOutcome.presentation(): Pair<String, String> = when (this) {
    PaymentOutcome.Approved -> "Pagamento aprovado" to "Compra confirmada com sucesso."
    PaymentOutcome.Declined -> "Pagamento não aprovado" to "Nenhuma cobrança adicional foi enviada. Tente novamente."
    PaymentOutcome.Canceled -> "Pagamento cancelado" to "A tentativa foi cancelada com segurança."
    PaymentOutcome.TechnicalError -> "Erro técnico" to "Não foi possível concluir. Tente novamente."
    PaymentOutcome.Pending -> "Pagamento pendente" to "A tentativa ainda está aguardando confirmação."
}
