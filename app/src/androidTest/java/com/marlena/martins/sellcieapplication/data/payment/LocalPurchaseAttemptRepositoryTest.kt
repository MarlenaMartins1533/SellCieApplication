package com.marlena.martins.sellcieapplication.data.payment

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.domain.model.PaymentRequest
import com.marlena.martins.sellcieapplication.domain.model.PurchasedTicket
import com.marlena.martins.sellcieapplication.domain.repository.StartProcessingResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class LocalPurchaseAttemptRepositoryTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before fun resetDatabase() { context.deleteDatabase("purchases.db") }

    @Test
    fun storesAndRestoresFinalStatusesByUniquePurchaseId() = runBlocking {
        val repository = LocalPurchaseAttemptRepository(context)
        val outcomes = listOf(PaymentOutcome.Approved, PaymentOutcome.Declined, PaymentOutcome.Canceled, PaymentOutcome.TechnicalError)
        outcomes.forEachIndexed { index, outcome ->
            val request = PaymentRequest("purchase-$index", 2500, "music", 1)
            assertEquals(StartProcessingResult.Started::class, repository.startProcessing(request)::class)
            repository.complete(request.purchaseId, outcome)
            assertEquals(outcome, repository.get(request.purchaseId)?.outcome)
            assertEquals(StartProcessingResult.AlreadyCompleted(outcome), repository.startProcessing(request))
        }
    }

    @Test
    fun persistsMetadataAndRecoversIt() = runBlocking {
        val repository = LocalPurchaseAttemptRepository(context)
        val request = PaymentRequest("purchase-meta", 1000)
        val metadata = mapOf(
            CieloConstants.KEY_BRAND to "Mastercard",
            CieloConstants.KEY_CIELO_CODE to "123456"
        )
        
        repository.startProcessing(request)
        repository.complete(request.purchaseId, PaymentOutcome.Approved, metadata)
        
        val recovered = repository.get(request.purchaseId)
        assertEquals(metadata, recovered?.cieloMetadata)
    }

    @Test
    fun persistsEveryItemInThePurchaseReceipt() = runBlocking {
        val repository = LocalPurchaseAttemptRepository(context)
        val request = PaymentRequest(
            purchaseId = "purchase-items",
            totalInCents = 6800,
            items = listOf(
                PurchasedTicket("music", "Music", 2, 2500),
                PurchasedTicket("tech", "Tech", 1, 1800)
            )
        )

        repository.startProcessing(request)
        repository.complete(request.purchaseId, PaymentOutcome.Approved)

        assertEquals(request.items, repository.get(request.purchaseId)?.items)
    }
}
