package com.marlena.martins.sellcieapplication.domain.repository

import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.domain.model.PaymentRequest
import com.marlena.martins.sellcieapplication.domain.model.PurchaseAttempt

interface PurchaseAttemptRepository {
    suspend fun startProcessing(request: PaymentRequest): StartProcessingResult

    suspend fun complete(purchaseId: String, outcome: PaymentOutcome): PurchaseAttempt

    suspend fun get(purchaseId: String): PurchaseAttempt?
}

sealed interface StartProcessingResult {
    data class Started(val attempt: PurchaseAttempt) : StartProcessingResult
    data object AlreadyProcessing : StartProcessingResult
    data class AlreadyCompleted(val outcome: PaymentOutcome) : StartProcessingResult
}
