package com.marlena.martins.sellcieapplication.data.events

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.marlena.martins.sellcieapplication.domain.model.PurchasedTicket
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalEventRepositoryTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    @After
    fun resetInventory() {
        context.getSharedPreferences("ticket_inventory", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun lowersLocalAvailabilityOnlyOnceForAnApprovedPurchase() {
        val repository = LocalEventRepository(context)
        val initialAvailability = repository.getEvents().first { it.id == "cielo-music-2026" }.availableTickets
        val items = listOf(PurchasedTicket("cielo-music-2026", "Cielo Music Experience", 2, 2500))

        repository.recordApprovedPurchase("approved-purchase", items)
        repository.recordApprovedPurchase("approved-purchase", items)

        val reloadedRepository = LocalEventRepository(context)
        val availability = reloadedRepository.getEvents()
            .first { it.id == "cielo-music-2026" }
            .availableTickets
        assertEquals(initialAvailability - 2, availability)
    }

    @Test
    fun createsEventsAndAdjustsTheirAvailableTicketsWithoutGoingBelowZero() {
        val repository = LocalEventRepository(context)
        repository.createEvent(
            com.marlena.martins.sellcieapplication.domain.model.Event(
                id = "custom-event",
                title = "Evento cadastrado",
                date = "10 de outubro • 19h",
                location = "São Paulo",
                priceInCents = 4590,
                availableTickets = 2
            )
        )

        repository.adjustAvailableTickets("custom-event", -3)
        repository.adjustAvailableTickets("custom-event", 4)

        val persistedEvent = LocalEventRepository(context).getEvents().first { it.id == "custom-event" }
        assertEquals(4, persistedEvent.availableTickets)
        assertEquals(4590L, persistedEvent.priceInCents)
    }
}
