package com.marlena.martins.sellcieapplication.domain.repository

import com.marlena.martins.sellcieapplication.domain.model.Event
import com.marlena.martins.sellcieapplication.domain.model.PurchasedTicket

interface EventRepository {
    fun getEvents(): List<Event>

    fun createEvent(event: Event) = Unit

    fun adjustAvailableTickets(eventId: String, delta: Int) = Unit

    /**
     * Registra a baixa definitiva de uma compra aprovada. Implementações devem ser idempotentes
     * por [purchaseId] para impedir redução duplicada caso a confirmação seja reprocessada.
     */
    fun recordApprovedPurchase(purchaseId: String, items: List<PurchasedTicket>) = Unit
}
