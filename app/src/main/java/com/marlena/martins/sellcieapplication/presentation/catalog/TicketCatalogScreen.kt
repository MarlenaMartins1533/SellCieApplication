package com.marlena.martins.sellcieapplication.presentation.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.marlena.martins.sellcieapplication.presentation.catalog.components.EventTicketCard
import com.marlena.martins.sellcieapplication.presentation.catalog.components.CieloAppHeader
import com.marlena.martins.sellcieapplication.presentation.catalog.components.OrderSummaryCard
import com.marlena.martins.sellcieapplication.presentation.checkout.CheckoutScreen
import com.marlena.martins.sellcieapplication.presentation.checkout.PurchasedTicketsScreen
import com.marlena.martins.sellcieapplication.presentation.checkout.ReceiptScreen


@Composable
fun TicketCatalogRoute(viewModel: TicketViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    when (uiState.screen) {
        TicketScreen.CATALOG -> TicketCatalogScreen(
            uiState = uiState,
            onQuantityChange = viewModel::changeQuantity,
            onContinue = viewModel::onContinue
        )

        TicketScreen.CHECKOUT -> CheckoutScreen(
            selectedTicketCount = uiState.selectedTicketCount,
            totalInCents = uiState.totalInCents,
            paymentState = uiState.paymentState,
            onConfirm = viewModel::confirmPurchase,
            onBack = viewModel::backToCatalog
        )

        TicketScreen.RECEIPT -> ReceiptScreen(
            receipt = uiState.receipt,
            outcome = (uiState.paymentState as? PaymentUiState.Result)?.outcome,
            onViewTickets = viewModel::showMyTickets,
            onBack = viewModel::backToCatalog
        )

        TicketScreen.MY_TICKETS -> PurchasedTicketsScreen(
            receipt = uiState.receipt,
            onBack = viewModel::backToCatalog
        )
    }
}

@Composable
fun TicketCatalogScreen(
    uiState: TicketUiState,
    onQuantityChange: (String, Int) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.notice, uiState.errorMessage) {
        (uiState.notice ?: uiState.errorMessage)?.let { message ->
            snackbarHostState.showSnackbar(message = message)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CieloAppHeader(
                title = "Cielo Ingressos",
                subtitle = "Compra rápida e segura para os seus eventos"
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Surface(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Ingressos",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Escolha seus eventos e monte seu pedido.",
                        modifier = Modifier.padding(top = 4.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(uiState.events, key = { it.id }) { event ->
                    EventTicketCard(
                        event = event,
                        quantity = uiState.quantityFor(event.id),
                        onDecrease = { onQuantityChange(event.id, -1) },
                        onIncrease = { onQuantityChange(event.id, 1) }
                    )
                }

                item {
                    OrderSummaryCard(
                        selectedTicketCount = uiState.selectedTicketCount,
                        totalInCents = uiState.totalInCents
                    )
                }

                item {
                    Button(
                        onClick = onContinue,
                        enabled = uiState.selectedTicketCount > 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { testTag = CatalogTestTags.CONTINUE_BUTTON }
                    ) {
                        Text("Continuar")
                    }
                }
            }
        }
    }
}
