package com.marlena.martins.sellcieapplication.data.payment

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.domain.model.PaymentRequest
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
}
