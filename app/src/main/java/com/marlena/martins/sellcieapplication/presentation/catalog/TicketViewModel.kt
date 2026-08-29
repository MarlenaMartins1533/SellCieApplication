package com.marlena.martins.sellcieapplication.presentation.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marlena.martins.sellcieapplication.R
import com.marlena.martins.sellcieapplication.domain.model.CartItem
import com.marlena.martins.sellcieapplication.domain.model.Event
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

    fun openInventory() {
        val state = mutableUiState.value
        if (state.paymentState is PaymentUiState.Processing) return
        mutableUiState.value = state.copy(
            screen = TicketScreen.INVENTORY,
            inventoryDraftEvents = state.events,
            notice = null
        )
    }

    fun adjustInventory(eventId: String, delta: Int) {
        if (delta != 0) updateInventoryQuantity(eventId) { (it + delta).coerceAtLeast(0) }
    }

    fun setInventoryQuantity(eventId: String, quantity: Int) {
        if (quantity >= 0) updateInventoryQuantity(eventId) { quantity }
    }

    fun createEvent(
        title: String,
        date: String,
        location: String,
        priceInCents: Long,
        availableTickets: Int
    ): Boolean = runCatching {
        val state = mutableUiState.value
        check(state.screen == TicketScreen.INVENTORY)
        require(title.isNotBlank() && date.isNotBlank() && location.isNotBlank())
        require(priceInCents >= 0 && availableTickets >= 0)
        val event = Event(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            date = date.trim(),
            location = location.trim(),
            priceInCents = priceInCents,
            availableTickets = availableTickets
        )
        eventRepository.createEvent(event)
        val persistedEvents = eventRepository.getEvents()
        val persistedEvent = requireNotNull(persistedEvents.firstOrNull { it.id == event.id })
        mutableUiState.value = state.copy(
            events = persistedEvents,
            inventoryDraftEvents = state.inventoryDraftEvents + persistedEvent,
            notice = UiText.StringResource(R.string.inventory_notice_created)
        )
        true
    }.getOrDefault(false)

    fun saveInventory(): Boolean {
        val state = mutableUiState.value
        if (state.screen != TicketScreen.INVENTORY) return false
        return runCatching {
            state.inventoryDraftEvents.forEach { draftEvent ->
                val storedEvent = state.events.firstOrNull { it.id == draftEvent.id }
                if (storedEvent == null) {
                    eventRepository.createEvent(draftEvent)
                } else {
                    eventRepository.adjustAvailableTickets(
                        draftEvent.id,
                        draftEvent.availableTickets - storedEvent.availableTickets
                    )
                }
            }
            eventRepository.getEvents()
        }.fold(
            onSuccess = { savedEvents ->
                val quantities = state.quantitiesByEventId.mapNotNull { (eventId, quantity) ->
                    savedEvents.firstOrNull { it.id == eventId }
                        ?.let { event -> eventId to quantity.coerceAtMost(event.availableTickets) }
                        ?.takeIf { (_, adjustedQuantity) -> adjustedQuantity > 0 }
                }.toMap()
                mutableUiState.value = state.copy(
                    events = savedEvents,
                    inventoryDraftEvents = savedEvents,
                    quantitiesByEventId = quantities,
                    totalInCents = calculateTotal(savedEvents, quantities),
                    notice = UiText.StringResource(R.string.inventory_notice_saved)
                )
                true
            },
            onFailure = {
                mutableUiState.value = state.copy(
                    notice = UiText.StringResource(R.string.inventory_error_save)
                )
                false
            }
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
                val events = if (result.outcome == PaymentOutcome.Approved) {
                    eventRepository.recordApprovedPurchase(request.purchaseId, purchasedItems)
                    eventRepository.getEvents()
                } else {
                    mutableUiState.value.events
                }
                val receipt = getPurchaseReceipt?.invoke(request.purchaseId)
                mutableUiState.value = mutableUiState.value.copy(
                    screen = TicketScreen.RECEIPT,
                    paymentState = PaymentUiState.Result(result.outcome),
                    receipt = receipt,
                    events = events
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
                onFailure = { TicketUiState(
                    errorMessage = UiText.StringResource(R.string.catalog_error_load)
                ) }
            )
    }

    private fun updateInventoryQuantity(eventId: String, update: (Int) -> Int) {
        val state = mutableUiState.value
        if (state.screen != TicketScreen.INVENTORY) return
        val draftEvents = state.inventoryDraftEvents.map { event ->
            if (event.id == eventId) event.copy(availableTickets = update(event.availableTickets)) else event
        }
        mutableUiState.value = state.copy(inventoryDraftEvents = draftEvents)
    }

    private fun calculateTotal(
        events: List<Event>,
        quantities: Map<String, Int>
    ): Long = calculateOrderTotal(
        events.mapNotNull { event ->
            quantities[event.id]?.takeIf { it > 0 }?.let { CartItem(event, it) }
        }
    )
}
