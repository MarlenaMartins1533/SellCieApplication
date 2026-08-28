package com.marlena.martins.sellcieapplication.presentation.catalog

import com.marlena.martins.sellcieapplication.domain.model.Event
import com.marlena.martins.sellcieapplication.data.payment.InMemoryPurchaseAttemptRepository
import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.domain.repository.EventRepository
import com.marlena.martins.sellcieapplication.domain.repository.PaymentGateway
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
                    ): PaymentOutcome {
                        gatewayCalls += 1
                        return PaymentOutcome.Approved
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
            ) = PaymentOutcome.Approved
        }
    )

    private class FakeEventRepository(
        private val events: List<Event>
    ) : EventRepository {
        override fun getEvents(): List<Event> = events
    }
}
