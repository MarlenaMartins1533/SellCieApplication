package com.marlena.martins.sellcieapplication.presentation.catalog

import androidx.lifecycle.ViewModel
import com.marlena.martins.sellcieapplication.domain.model.CartItem
import com.marlena.martins.sellcieapplication.domain.repository.EventRepository
import com.marlena.martins.sellcieapplication.domain.usecase.CalculateOrderTotal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TicketViewModel(
    private val eventRepository: EventRepository,
    private val calculateOrderTotal: CalculateOrderTotal
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
        if (mutableUiState.value.selectedTicketCount == 0) return
        mutableUiState.value = mutableUiState.value.copy(
            notice = "Resumo pronto. O pagamento será implementado na próxima etapa."
        )
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
