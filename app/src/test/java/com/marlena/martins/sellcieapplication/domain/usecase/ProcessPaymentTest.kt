package com.marlena.martins.sellcieapplication.domain.usecase

import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.domain.model.PaymentRequest
import com.marlena.martins.sellcieapplication.domain.model.PaymentSimulation
import com.marlena.martins.sellcieapplication.domain.repository.PaymentGateway
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
        val request = PaymentRequest("purchase-1", 2500, PaymentSimulation.DECLINED)

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

        override suspend fun process(request: PaymentRequest): PaymentOutcome {
            callCount += 1
            return outcome
        }
    }

    private class FakePurchaseAttemptRepository(
        private var nextStartResult: StartProcessingResult? = null
    ) : PurchaseAttemptRepository {
        private var finalOutcome: PaymentOutcome? = null

        override fun startProcessing(request: PaymentRequest): StartProcessingResult {
            nextStartResult?.let { return it }
            finalOutcome?.let { return StartProcessingResult.AlreadyCompleted(it) }
            return StartProcessingResult.Started(
                com.marlena.martins.sellcieapplication.domain.model.PurchaseAttempt(
                    purchaseId = request.purchaseId,
                    totalInCents = request.totalInCents,
                    simulation = request.simulation,
                    status = com.marlena.martins.sellcieapplication.domain.model.PurchaseAttemptStatus.PROCESSING
                )
            )
        }

        override fun complete(purchaseId: String, outcome: PaymentOutcome) =
            com.marlena.martins.sellcieapplication.domain.model.PurchaseAttempt(
                purchaseId = purchaseId,
                totalInCents = 2500,
                simulation = PaymentSimulation.APPROVED,
                status = outcome.toStatus(),
                outcome = outcome
            ).also { finalOutcome = outcome }

        private fun PaymentOutcome.toStatus() = when (this) {
            PaymentOutcome.Approved -> com.marlena.martins.sellcieapplication.domain.model.PurchaseAttemptStatus.APPROVED
            PaymentOutcome.Declined -> com.marlena.martins.sellcieapplication.domain.model.PurchaseAttemptStatus.DECLINED
            PaymentOutcome.Canceled -> com.marlena.martins.sellcieapplication.domain.model.PurchaseAttemptStatus.CANCELED
            PaymentOutcome.TechnicalError -> com.marlena.martins.sellcieapplication.domain.model.PurchaseAttemptStatus.TECHNICAL_ERROR
        }
    }
}
