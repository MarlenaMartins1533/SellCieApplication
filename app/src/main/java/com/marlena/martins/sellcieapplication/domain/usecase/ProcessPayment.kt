package com.marlena.martins.sellcieapplication.domain.usecase

import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.domain.model.PaymentRequest
import com.marlena.martins.sellcieapplication.domain.repository.PaymentGateway
import com.marlena.martins.sellcieapplication.domain.repository.PaymentGatewayResult
import com.marlena.martins.sellcieapplication.domain.repository.PurchaseAttemptRepository
import com.marlena.martins.sellcieapplication.domain.repository.StartProcessingResult

class ProcessPayment(
    private val purchaseAttemptRepository: PurchaseAttemptRepository,
    private val paymentGateway: PaymentGateway
) {
    suspend operator fun invoke(request: PaymentRequest): ProcessPaymentResult = when (
        val startResult = purchaseAttemptRepository.startProcessing(request)
    ) {
        is StartProcessingResult.Started -> {
            val result = runCatching { paymentGateway.process(request) }
                .getOrElse { PaymentGatewayResult(PaymentOutcome.TechnicalError) }
            purchaseAttemptRepository.complete(request.purchaseId, result.outcome, result.metadata)
            ProcessPaymentResult.Completed(result.outcome)
        }

        StartProcessingResult.AlreadyProcessing -> ProcessPaymentResult.AlreadyProcessing
        is StartProcessingResult.AlreadyCompleted -> ProcessPaymentResult.Completed(startResult.outcome)
    }
}

sealed interface ProcessPaymentResult {
    data object AlreadyProcessing : ProcessPaymentResult
    data class Completed(val outcome: PaymentOutcome) : ProcessPaymentResult
}
