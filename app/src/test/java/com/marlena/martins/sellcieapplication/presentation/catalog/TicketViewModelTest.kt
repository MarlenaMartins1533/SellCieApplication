package com.marlena.martins.sellcieapplication.presentation.catalog

import com.marlena.martins.sellcieapplication.domain.model.Event
import com.marlena.martins.sellcieapplication.domain.repository.EventRepository
import com.marlena.martins.sellcieapplication.domain.usecase.CalculateOrderTotal
import org.junit.Assert.assertEquals
import org.junit.Test

class TicketViewModelTest {

    @Test
    fun `updates quantities independently and recalculates total`() {
        val viewModel = TicketViewModel(
            eventRepository = FakeEventRepository(events()),
            calculateOrderTotal = CalculateOrderTotal()
        )

        viewModel.changeQuantity("music", 2)
        viewModel.changeQuantity("tech", 2)

        val state = viewModel.uiState.value
        assertEquals(2, state.quantityFor("music"))
        assertEquals(2, state.quantityFor("tech"))
        assertEquals(8600L, state.totalInCents)
    }

    @Test
    fun `does not exceed the available ticket quantity`() {
        val viewModel = TicketViewModel(
            eventRepository = FakeEventRepository(events()),
            calculateOrderTotal = CalculateOrderTotal()
        )

        viewModel.changeQuantity("tech", 5)

        assertEquals(2, viewModel.uiState.value.quantityFor("tech"))
        assertEquals(3600L, viewModel.uiState.value.totalInCents)
    }

    private fun events() = listOf(
        Event("music", "Music", "data", "local", 2500, 8),
        Event("tech", "Tech", "data", "local", 1800, 2)
    )

    private class FakeEventRepository(
        private val events: List<Event>
    ) : EventRepository {
        override fun getEvents(): List<Event> = events
    }
}
