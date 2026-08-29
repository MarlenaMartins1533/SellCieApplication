package com.marlena.martins.sellcieapplication.domain.usecase

import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.domain.model.PaymentRequest
import com.marlena.martins.sellcieapplication.domain.repository.PaymentGateway
import com.marlena.martins.sellcieapplication.domain.repository.PaymentGatewayResult
import com.marlena.martins.sellcieapplication.domain.repository.PurchaseAttemptRepository
import com.marlena.martins.sellcieapplication.domain.repository.StartProcessingResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ProcessPaymentTest {

    @Test
    fun `reuses the final outcome without calling the gateway again`() = runBlocking {
        val repository = FakePurchaseAttemptRepository()
        val gateway = CountingGateway(PaymentOutcome.Declined)
        val useCase = ProcessPayment(repository, gateway)
        val request = PaymentRequest("purchase-1", 2500)

        assertEquals(ProcessPaymentResult.Completed(PaymentOutcome.Declined), useCase(request))
        assertEquals(ProcessPaymentResult.Completed(PaymentOutcome.Declined), useCase(request))

        assertEquals(1, gateway.callCount)
    }

    @Test
    fun `does not call gateway when the same purchase is already processing`() = runBlocking {
        val repository = FakePurchaseAttemptRepository(
            nextStartResult = StartProcessingResult.AlreadyProcessing
        )
        val gateway = CountingGateway(PaymentOutcome.Approved)

        val result = ProcessPayment(repository, gateway)(PaymentRequest("purchase-2", 2500))

        assertEquals(ProcessPaymentResult.AlreadyProcessing, result)
        assertEquals(0, gateway.callCount)
    }

    private class CountingGateway(
        private val outcome: PaymentOutcome
    ) : PaymentGateway {
        var callCount = 0

        override suspend fun process(request: PaymentRequest): PaymentGatewayResult {
            callCount += 1
            return PaymentGatewayResult(outcome)
        }
    }

    private class FakePurchaseAttemptRepository(
        private var nextStartResult: StartProcessingResult? = null
    ) : PurchaseAttemptRepository {
        private var finalOutcome: PaymentOutcome? = null

        override suspend fun startProcessing(request: PaymentRequest): StartProcessingResult {
            nextStartResult?.let { return it }
            finalOutcome?.let { return StartProcessingResult.AlreadyCompleted(it) }
            return StartProcessingResult.Started(
                com.marlena.martins.sellcieapplication.domain.model.PurchaseAttempt(
                    purchaseId = request.purchaseId,
                    totalInCents = request.totalInCents,
                    status = com.marlena.martins.sellcieapplication.domain.model.PurchaseAttemptStatus.PROCESSING
                )
            )
        }

        override suspend fun complete(purchaseId: String, outcome: PaymentOutcome, metadata: Map<String, String>) =
            com.marlena.martins.sellcieapplication.domain.model.PurchaseAttempt(
                purchaseId = purchaseId,
                totalInCents = 2500,
                status = outcome.toStatus(),
                outcome = outcome,
                cieloMetadata = metadata
            ).also { finalOutcome = outcome }

        private fun PaymentOutcome.toStatus() = when (this) {
            PaymentOutcome.Approved -> com.marlena.martins.sellcieapplication.domain.model.PurchaseAttemptStatus.APPROVED
            PaymentOutcome.Pending -> com.marlena.martins.sellcieapplication.domain.model.PurchaseAttemptStatus.PENDING
            PaymentOutcome.Declined -> com.marlena.martins.sellcieapplication.domain.model.PurchaseAttemptStatus.DECLINED
            PaymentOutcome.Canceled -> com.marlena.martins.sellcieapplication.domain.model.PurchaseAttemptStatus.CANCELED
            PaymentOutcome.TechnicalError -> com.marlena.martins.sellcieapplication.domain.model.PurchaseAttemptStatus.TECHNICAL_ERROR
        }

        override suspend fun get(purchaseId: String) = finalOutcome?.let {
            com.marlena.martins.sellcieapplication.domain.model.PurchaseAttempt(
                purchaseId = purchaseId, totalInCents = 2500, outcome = it,
                status = it.toStatus()
            )
        }
    }
}
