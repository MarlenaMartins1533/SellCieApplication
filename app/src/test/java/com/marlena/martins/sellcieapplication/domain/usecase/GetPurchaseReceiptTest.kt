package com.marlena.martins.sellcieapplication.domain.usecase

import com.marlena.martins.sellcieapplication.data.payment.InMemoryPurchaseAttemptRepository
import com.marlena.martins.sellcieapplication.domain.model.Event
import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.domain.model.PaymentRequest
import com.marlena.martins.sellcieapplication.domain.model.PurchasedTicket
import com.marlena.martins.sellcieapplication.domain.repository.EventRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GetPurchaseReceiptTest {
    @Test
    fun `maps every persisted item to a receipt with stable short reference`() = runBlocking {
        val repository = InMemoryPurchaseAttemptRepository()
        val request = PaymentRequest(
            purchaseId = "purchase-12345678",
            totalInCents = 8600,
            items = listOf(
                PurchasedTicket("music", "Music", 2, 2500),
                PurchasedTicket("tech", "Tech", 2, 1800)
            )
        )
        repository.startProcessing(request)
        repository.complete(request.purchaseId, PaymentOutcome.Approved)

        val receipt = GetPurchaseReceipt(repository, FakeEvents())(request.purchaseId)

        assertEquals(listOf("Music", "Tech"), receipt?.items?.map { it.title })
        assertEquals(4, receipt?.totalQuantity)
        assertEquals(8600L, receipt?.totalInCents)
        assertEquals("12345678", receipt?.shortReference)
        org.junit.Assert.assertTrue(receipt?.formattedDate?.isNotBlank() == true)
    }

    private class FakeEvents : EventRepository {
        override fun getEvents() = listOf(Event("music", "Music", "18 set", "São Paulo", 2500, 10))
    }
}
