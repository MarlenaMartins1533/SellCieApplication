package com.marlena.martins.sellcieapplication.domain.usecase

import com.marlena.martins.sellcieapplication.domain.model.CartItem
import com.marlena.martins.sellcieapplication.domain.model.Event
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateOrderTotalTest {

    private val useCase = CalculateOrderTotal()

    @Test
    fun `calculates total in cents from selected items`() {
        val music = event(id = "music", priceInCents = 2500)
        val tech = event(id = "tech", priceInCents = 1800)

        val total = useCase(listOf(CartItem(music, 2), CartItem(tech, 1)))

        assertEquals(6800L, total)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `does not accept an item with zero quantity`() {
        CartItem(event(id = "music", priceInCents = 2500), 0)
    }

    private fun event(id: String, priceInCents: Long) = Event(
        id = id,
        title = id,
        date = "data",
        location = "local",
        priceInCents = priceInCents,
        availableTickets = 8
    )
}
