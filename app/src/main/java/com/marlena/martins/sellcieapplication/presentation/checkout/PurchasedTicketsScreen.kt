package com.marlena.martins.sellcieapplication.presentation.checkout

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.marlena.martins.sellcieapplication.domain.usecase.PurchaseReceipt
import com.marlena.martins.sellcieapplication.presentation.catalog.CatalogTestTags
import com.marlena.martins.sellcieapplication.presentation.catalog.components.CieloAppHeader
import qrcode.QRCode

@Composable
fun PurchasedTicketsScreen(
    receipt: PurchaseReceipt?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(topBar = { CieloAppHeader("SellCie", "Ingressos comprados") }) { padding ->
        if (receipt == null) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Não foi possível recuperar os ingressos desta compra.")
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text("Meus ingressos", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Cada cartão representa um ingresso da sua compra.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(receipt.individualTickets(), key = { it.id }) { ticket ->
                    // Generate a basic QR code object
                    val qrCode: ByteArray = QRCode(ticket.qrPayload).render().getBytes()

                    Card(modifier = Modifier.fillMaxWidth()) {
                        androidx.compose.foundation.layout.Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                ticket.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Ingresso ${ticket.number} de ${ticket.quantityForEvent}")
                            QrCodeField(qrCode, ticket.qrPayload)
                        }
                    }
                }
                item {
                    Button(
                        onClick = onBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { testTag = CatalogTestTags.MY_TICKETS_BACK_BUTTON }
                    ) { Text("Voltar ao catálogo") }
                }
            }
        }
    }
}

@Composable
private fun QrCodeField(qrCode: ByteArray, payload: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .semantics { testTag = "ticket_qr_$payload" },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    val imageBitmap = remember(qrCode) {
                        BitmapFactory.decodeByteArray(qrCode, 0, qrCode.size)?.asImageBitmap()
                    }

                    if (imageBitmap != null) {
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = payload,
                            modifier = Modifier.size(180.dp) // Defina o tamanho desejado
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "QR Code do ingresso: \n$payload",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxSize().size(24.dp)
                    )
                }
            }
        }
    }
}

private data class IndividualTicket(
    val id: String,
    val title: String,
    val number: Int,
    val quantityForEvent: Int,
    val qrPayload: String
)

private fun PurchaseReceipt.individualTickets(): List<IndividualTicket> = items.flatMap { item ->
    (1..item.quantity).map { number ->
        val payload = "$purchaseId-${item.eventId}-$number"
        IndividualTicket(
            id = payload,
            title = item.title,
            number = number,
            quantityForEvent = item.quantity,
            qrPayload = payload
        )
    }
}
