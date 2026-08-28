package com.marlena.martins.sellcieapplication.domain.usecase

import com.marlena.martins.sellcieapplication.data.payment.InMemoryPurchaseAttemptRepository
import com.marlena.martins.sellcieapplication.domain.model.Event
import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.domain.model.PaymentRequest
import com.marlena.martins.sellcieapplication.domain.repository.EventRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GetPurchaseReceiptTest {
    @Test
    fun `maps persisted attempt to receipt with stable short reference`() = runBlocking {
        val repository = InMemoryPurchaseAttemptRepository()
        val request = PaymentRequest("purchase-12345678", 5000, "music", 2)
        repository.startProcessing(request)
        repository.complete(request.purchaseId, PaymentOutcome.Approved)

        val receipt = GetPurchaseReceipt(repository, FakeEvents())(request.purchaseId)

        assertEquals("Music", receipt?.event?.title)
        assertEquals(2, receipt?.quantity)
        assertEquals(5000L, receipt?.totalInCents)
        assertEquals("12345678", receipt?.shortReference)
        org.junit.Assert.assertTrue(receipt?.formattedDate?.isNotBlank() == true)
    }

    private class FakeEvents : EventRepository {
        override fun getEvents() = listOf(Event("music", "Music", "18 set", "São Paulo", 2500, 10))
    }
}
