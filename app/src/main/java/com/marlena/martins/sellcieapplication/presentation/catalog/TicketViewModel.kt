package com.marlena.martins.sellcieapplication.presentation.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marlena.martins.sellcieapplication.domain.model.CartItem
import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.domain.model.PaymentRequest
import com.marlena.martins.sellcieapplication.domain.model.PurchasedTicket
import com.marlena.martins.sellcieapplication.domain.repository.EventRepository
import com.marlena.martins.sellcieapplication.domain.usecase.CalculateOrderTotal
import com.marlena.martins.sellcieapplication.domain.usecase.ProcessPayment
import com.marlena.martins.sellcieapplication.domain.usecase.ProcessPaymentResult
import com.marlena.martins.sellcieapplication.domain.usecase.GetPurchaseReceipt
import java.util.UUID
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TicketViewModel(
    private val eventRepository: EventRepository,
    private val calculateOrderTotal: CalculateOrderTotal,
    private val processPayment: ProcessPayment,
    private val getPurchaseReceipt: GetPurchaseReceipt? = null
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(TicketUiState())
    val uiState: StateFlow<TicketUiState> = mutableUiState.asStateFlow()

    init {
        loadEvents()
    }

    fun changeQuantity(eventId: String, delta: Int) {
        val currentState = mutableUiState.value
        val event = currentState.events.firstOrNull { it.id == eventId } ?: return
        val nextQuantity = (currentState.quantityFor(eventId) + delta)
            .coerceIn(0, event.availableTickets)
        if (nextQuantity == currentState.quantityFor(eventId)) return

        val nextQuantities = currentState.quantitiesByEventId.toMutableMap().apply {
            if (nextQuantity == 0) remove(eventId) else put(eventId, nextQuantity)
        }
        mutableUiState.value = currentState.copy(
            quantitiesByEventId = nextQuantities,
            totalInCents = calculateTotal(currentState.events, nextQuantities),
            notice = null
        )
    }

    fun onContinue() {
        val state = mutableUiState.value
        if (state.selectedTicketCount == 0 || state.paymentState is PaymentUiState.Processing) return

        mutableUiState.value = state.copy(
            screen = TicketScreen.CHECKOUT,
            paymentState = PaymentUiState.Idle,
            notice = null
        )
    }

    fun confirmPurchase() {
        val state = mutableUiState.value
        if (
            state.screen != TicketScreen.CHECKOUT ||
            state.selectedTicketCount == 0 ||
            state.paymentState is PaymentUiState.Processing
        ) return

        val purchasedItems = state.events.mapNotNull { event ->
            state.quantityFor(event.id).takeIf { it > 0 }?.let { quantity ->
                PurchasedTicket(
                    eventId = event.id,
                    title = event.title,
                    quantity = quantity,
                    unitPriceInCents = event.priceInCents
                )
            }
        }
        val request = PaymentRequest(
            // MerchantOrderId aceita somente caracteres alfanuméricos na API Cielo.
            purchaseId = UUID.randomUUID().toString().replace("-", ""),
            totalInCents = state.totalInCents,
            items = purchasedItems
        )
        mutableUiState.value = state.copy(
            paymentState = PaymentUiState.Processing(request.purchaseId),
            notice = null
        )

        viewModelScope.launch {
            val result = runCatching { processPayment(request) }
                .getOrElse { ProcessPaymentResult.Completed(PaymentOutcome.TechnicalError) }
            if (result is ProcessPaymentResult.Completed) {
                val receipt = getPurchaseReceipt?.invoke(request.purchaseId)
                mutableUiState.value = mutableUiState.value.copy(
                    screen = TicketScreen.RECEIPT,
                    paymentState = PaymentUiState.Result(result.outcome),
                    receipt = receipt
                )
            }
        }
    }

    fun showMyTickets() {
        val state = mutableUiState.value
        val outcome = (state.paymentState as? PaymentUiState.Result)?.outcome
        if (state.screen != TicketScreen.RECEIPT || outcome != PaymentOutcome.Approved || state.receipt == null) return

        mutableUiState.value = state.copy(screen = TicketScreen.MY_TICKETS)
    }

    fun backToCatalog() {
        if (mutableUiState.value.paymentState is PaymentUiState.Processing) return
        mutableUiState.value = mutableUiState.value.returnToCatalog()
    }

    private fun loadEvents() {
        mutableUiState.value = runCatching { eventRepository.getEvents() }
            .fold(
                onSuccess = { events -> TicketUiState(events = events) },
                onFailure = { TicketUiState(errorMessage = "Não foi possível carregar os eventos locais.") }
            )
    }

    private fun calculateTotal(
        events: List<com.marlena.martins.sellcieapplication.domain.model.Event>,
        quantities: Map<String, Int>
    ): Long = calculateOrderTotal(
        events.mapNotNull { event ->
            quantities[event.id]?.takeIf { it > 0 }?.let { CartItem(event, it) }
        }
    )
}
