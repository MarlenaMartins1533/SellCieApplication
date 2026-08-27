package com.marlena.martins.sellcieapplication.presentation.catalog

import com.marlena.martins.sellcieapplication.domain.model.Event

data class TicketUiState(
    val events: List<Event> = emptyList(),
    val quantitiesByEventId: Map<String, Int> = emptyMap(),
    val totalInCents: Long = 0,
    val notice: String? = null,
    val errorMessage: String? = null
) {
    val selectedTicketCount: Int
        get() = quantitiesByEventId.values.sum()

    fun quantityFor(eventId: String): Int = quantitiesByEventId[eventId] ?: 0
}
