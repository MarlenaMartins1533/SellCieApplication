package com.marlena.martins.sellcieapplication.presentation.catalog

import com.marlena.martins.sellcieapplication.domain.model.Event
import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.domain.usecase.PurchaseReceipt

data class TicketUiState(
    val events: List<Event> = emptyList(),
    val quantitiesByEventId: Map<String, Int> = emptyMap(),
    val totalInCents: Long = 0,
    val screen: TicketScreen = TicketScreen.CATALOG,
    val paymentState: PaymentUiState = PaymentUiState.Idle,
    val receipt: PurchaseReceipt? = null,
    val notice: String? = null,
    val errorMessage: String? = null
) {
    val selectedTicketCount: Int
        get() = quantitiesByEventId.values.sum()

    fun quantityFor(eventId: String): Int = quantitiesByEventId[eventId] ?: 0

    fun returnToCatalog(): TicketUiState {
        val approvedPurchase = screen in setOf(TicketScreen.RECEIPT, TicketScreen.MY_TICKETS) &&
            (paymentState as? PaymentUiState.Result)?.outcome == PaymentOutcome.Approved

        return copy(
            screen = TicketScreen.CATALOG,
            paymentState = PaymentUiState.Idle,
            receipt = null,
            notice = null,
            quantitiesByEventId = if (approvedPurchase) emptyMap() else quantitiesByEventId,
            totalInCents = if (approvedPurchase) 0 else totalInCents
        )
    }
}

enum class TicketScreen {
    CATALOG,
    CHECKOUT,
    RECEIPT,
    MY_TICKETS
}

sealed interface PaymentUiState {
    data object Idle : PaymentUiState
    data class Processing(val purchaseId: String) : PaymentUiState
    data class Result(val outcome: PaymentOutcome) : PaymentUiState
}
