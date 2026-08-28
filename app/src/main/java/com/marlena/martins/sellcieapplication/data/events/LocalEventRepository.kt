package com.marlena.martins.sellcieapplication.data.events

import android.content.Context
import androidx.core.content.edit
import com.marlena.martins.sellcieapplication.domain.model.Event
import com.marlena.martins.sellcieapplication.domain.model.PurchasedTicket
import com.marlena.martins.sellcieapplication.domain.repository.EventRepository
import org.json.JSONArray
import org.json.JSONObject

class LocalEventRepository(
    private val context: Context
) : EventRepository {
    private val inventoryPreferences = context.applicationContext.getSharedPreferences(
        INVENTORY_PREFERENCES,
        Context.MODE_PRIVATE
    )

    override fun getEvents(): List<Event> {
        val content = context.assets.open(EVENTS_FILE).bufferedReader().use { it.readText() }
        val assetEvents = JSONArray(content).toEvents()
        return (assetEvents + customEvents()).map(::eventWithCurrentAvailability)
    }

    override fun createEvent(event: Event) {
        require(event.id.isNotBlank()) { "O evento precisa de um identificador." }
        require(event.title.isNotBlank()) { "O evento precisa de um nome." }
        require(event.priceInCents >= 0) { "O preço não pode ser negativo." }
        require(event.availableTickets >= 0) { "A quantidade não pode ser negativa." }
        synchronized(inventoryPreferences) {
            check(getEvents().none { it.id == event.id }) { "Já existe um evento com este identificador." }
            val events = JSONArray(inventoryPreferences.getString(CUSTOM_EVENTS_KEY, "[]"))
            events.put(event.toJson())
            inventoryPreferences.edit(commit = true) { putString(CUSTOM_EVENTS_KEY, events.toString()) }
        }
    }

    override fun adjustAvailableTickets(eventId: String, delta: Int) {
        if (delta == 0) return
        synchronized(inventoryPreferences) {
            val currentEvent = getEvents().firstOrNull { it.id == eventId } ?: return
            val appliedDelta = delta.coerceAtLeast(-currentEvent.availableTickets)
            if (appliedDelta == 0) return
            inventoryPreferences.edit(commit = true) {
                putInt(availabilityAdjustmentKey(eventId), availabilityAdjustmentFor(eventId) + appliedDelta)
            }
        }
    }

    override fun recordApprovedPurchase(purchaseId: String, items: List<PurchasedTicket>) {
        synchronized(inventoryPreferences) {
            if (inventoryPreferences.getBoolean(appliedPurchaseKey(purchaseId), false)) return

            inventoryPreferences.edit(commit = true) {
                putBoolean(appliedPurchaseKey(purchaseId), true)
                items.forEach { item ->
                    putInt(soldTicketsKey(item.eventId), soldTicketsFor(item.eventId) + item.quantity)
                }
            }
        }
    }

    private fun org.json.JSONObject.toEvent() = Event(
        id = getString("id"),
        title = getString("title"),
        date = getString("date"),
        location = getString("location"),
        priceInCents = getLong("priceInCents"),
        availableTickets = getInt("availableTickets")
    )

    private fun JSONArray.toEvents() = List(length()) { index -> getJSONObject(index).toEvent() }

    private fun customEvents() = JSONArray(inventoryPreferences.getString(CUSTOM_EVENTS_KEY, "[]")).toEvents()

    private fun eventWithCurrentAvailability(event: Event) = event.copy(
        availableTickets = (
            event.availableTickets - soldTicketsFor(event.id) + availabilityAdjustmentFor(event.id)
        ).coerceAtLeast(0)
    )

    private fun Event.toJson() = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("date", date)
        put("location", location)
        put("priceInCents", priceInCents)
        put("availableTickets", availableTickets)
    }

    private fun soldTicketsFor(eventId: String) = inventoryPreferences.getInt(soldTicketsKey(eventId), 0)

    private fun soldTicketsKey(eventId: String) = "sold_$eventId"

    private fun availabilityAdjustmentFor(eventId: String) = inventoryPreferences.getInt(
        availabilityAdjustmentKey(eventId),
        0
    )

    private fun availabilityAdjustmentKey(eventId: String) = "availability_adjustment_$eventId"

    private fun appliedPurchaseKey(purchaseId: String) = "applied_purchase_$purchaseId"

    private companion object {
        const val EVENTS_FILE = "events.json"
        const val INVENTORY_PREFERENCES = "ticket_inventory"
        const val CUSTOM_EVENTS_KEY = "custom_events"
    }
}
