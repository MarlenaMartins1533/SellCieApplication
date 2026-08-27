package com.marlena.martins.sellcieapplication.presentation.catalog

import com.marlena.martins.sellcieapplication.domain.model.Event
import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.domain.model.PaymentSimulation

data class TicketUiState(
    val events: List<Event> = emptyList(),
    val quantitiesByEventId: Map<String, Int> = emptyMap(),
    val totalInCents: Long = 0,
    val screen: TicketScreen = TicketScreen.CATALOG,
    val paymentState: PaymentUiState = PaymentUiState.Idle,
    val paymentSimulation: PaymentSimulation = PaymentSimulation.APPROVED,
    val notice: String? = null,
    val errorMessage: String? = null
) {
    val selectedTicketCount: Int
        get() = quantitiesByEventId.values.sum()

    fun quantityFor(eventId: String): Int = quantitiesByEventId[eventId] ?: 0
}

enum class TicketScreen {
    CATALOG,
    CHECKOUT
}

sealed interface PaymentUiState {
    data object Idle : PaymentUiState
    data class Processing(val purchaseId: String) : PaymentUiState
    data class Result(val outcome: PaymentOutcome) : PaymentUiState
}
