package com.marlena.martins.sellcieapplication.presentation.catalog

import com.marlena.martins.sellcieapplication.domain.model.Event
import com.marlena.martins.sellcieapplication.data.payment.InMemoryPurchaseAttemptRepository
import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.domain.repository.EventRepository
import com.marlena.martins.sellcieapplication.domain.repository.PaymentGateway
import com.marlena.martins.sellcieapplication.domain.repository.PaymentGatewayResult
import com.marlena.martins.sellcieapplication.domain.usecase.CalculateOrderTotal
import com.marlena.martins.sellcieapplication.domain.usecase.ProcessPayment
import org.junit.Assert.assertEquals
import org.junit.Test

class TicketViewModelTest {

    @Test
    fun `opening the confirmation does not process the payment`() {
        var gatewayCalls = 0
        val viewModel = TicketViewModel(
            eventRepository = FakeEventRepository(events()),
            calculateOrderTotal = CalculateOrderTotal(),
            processPayment = ProcessPayment(
                purchaseAttemptRepository = InMemoryPurchaseAttemptRepository(),
                paymentGateway = object : PaymentGateway {
                    override suspend fun process(
                        request: com.marlena.martins.sellcieapplication.domain.model.PaymentRequest
                    ): PaymentGatewayResult {
                        gatewayCalls += 1
                        return PaymentGatewayResult(PaymentOutcome.Approved)
                    }
                }
            )
        )
        viewModel.changeQuantity("music", 1)

        viewModel.onContinue()

        assertEquals(TicketScreen.CHECKOUT, viewModel.uiState.value.screen)
        assertEquals(PaymentUiState.Idle, viewModel.uiState.value.paymentState)
        assertEquals(0, gatewayCalls)
    }

    @Test
    fun `clears the selected tickets only after returning from an approved receipt`() {
        val selectedItems = mapOf("music" to 2, "tech" to 1)

        val approvedReturn = TicketUiState(
            quantitiesByEventId = selectedItems,
            totalInCents = 6800,
            screen = TicketScreen.RECEIPT,
            paymentState = PaymentUiState.Result(PaymentOutcome.Approved)
        ).returnToCatalog()
        val declinedReturn = TicketUiState(
            quantitiesByEventId = selectedItems,
            totalInCents = 6800,
            screen = TicketScreen.RECEIPT,
            paymentState = PaymentUiState.Result(PaymentOutcome.Declined)
        ).returnToCatalog()

        assertEquals(TicketScreen.CATALOG, approvedReturn.screen)
        assertEquals(0, approvedReturn.selectedTicketCount)
        assertEquals(0L, approvedReturn.totalInCents)
        assertEquals(selectedItems, declinedReturn.quantitiesByEventId)
        assertEquals(6800L, declinedReturn.totalInCents)

        val ticketsReturn = TicketUiState(
            quantitiesByEventId = selectedItems,
            totalInCents = 6800,
            screen = TicketScreen.MY_TICKETS,
            paymentState = PaymentUiState.Result(PaymentOutcome.Approved)
        ).returnToCatalog()
        assertEquals(0, ticketsReturn.selectedTicketCount)
        assertEquals(0L, ticketsReturn.totalInCents)
    }

    @Test
    fun `inventory edits are discarded on return and persisted only after save`() {
        val repository = FakeEventRepository(events())
        val viewModel = TicketViewModel(
            eventRepository = repository,
            calculateOrderTotal = CalculateOrderTotal(),
            processPayment = paymentProcessor()
        )

        viewModel.openInventory()
        viewModel.setInventoryQuantity("music", 5)
        assertEquals(5, viewModel.uiState.value.inventoryDraftEvents.first { it.id == "music" }.availableTickets)
        assertEquals(8, repository.getEvents().first { it.id == "music" }.availableTickets)

        viewModel.backToCatalog()
        assertEquals(8, viewModel.uiState.value.events.first { it.id == "music" }.availableTickets)

        viewModel.openInventory()
        viewModel.adjustInventory("music", -3)
        assertEquals(true, viewModel.saveInventory())
        assertEquals(5, repository.getEvents().first { it.id == "music" }.availableTickets)
        assertEquals(5, viewModel.uiState.value.events.first { it.id == "music" }.availableTickets)
    }

    @Test
    fun `creating an event persists it without saving stock adjustments`() {
        val repository = FakeEventRepository(events())
        val viewModel = TicketViewModel(
            eventRepository = repository,
            calculateOrderTotal = CalculateOrderTotal(),
            processPayment = paymentProcessor()
        )

        viewModel.openInventory()
        viewModel.adjustInventory("music", -2)
        val created = viewModel.createEvent("Novo evento", "20 set", "São Paulo", 2000, 4)

        assertEquals(true, created)
        assertEquals(4, repository.getEvents().first { it.title == "Novo evento" }.availableTickets)
        assertEquals(8, repository.getEvents().first { it.id == "music" }.availableTickets)
    }

    @Test
    fun `updates quantities independently and recalculates total`() {
        val viewModel = TicketViewModel(
            eventRepository = FakeEventRepository(events()),
            calculateOrderTotal = CalculateOrderTotal(),
            processPayment = paymentProcessor()
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
            calculateOrderTotal = CalculateOrderTotal(),
            processPayment = paymentProcessor()
        )

        viewModel.changeQuantity("tech", 5)

        assertEquals(2, viewModel.uiState.value.quantityFor("tech"))
        assertEquals(3600L, viewModel.uiState.value.totalInCents)
    }

    private fun events() = listOf(
        Event("music", "Music", "data", "local", 2500, 8),
        Event("tech", "Tech", "data", "local", 1800, 2)
    )

    private fun paymentProcessor() = ProcessPayment(
        purchaseAttemptRepository = InMemoryPurchaseAttemptRepository(),
        paymentGateway = object : PaymentGateway {
            override suspend fun process(
                request: com.marlena.martins.sellcieapplication.domain.model.PaymentRequest
            ) = PaymentGatewayResult(PaymentOutcome.Approved)
        }
    )

    private class FakeEventRepository(
        events: List<Event>
    ) : EventRepository {
        private var events = events

        override fun getEvents(): List<Event> = events

        override fun createEvent(event: Event) {
            events = events + event
        }

        override fun adjustAvailableTickets(eventId: String, delta: Int) {
            events = events.map { event ->
                if (event.id == eventId) {
                    event.copy(availableTickets = (event.availableTickets + delta).coerceAtLeast(0))
                } else {
                    event
                }
            }
        }
    }
}
