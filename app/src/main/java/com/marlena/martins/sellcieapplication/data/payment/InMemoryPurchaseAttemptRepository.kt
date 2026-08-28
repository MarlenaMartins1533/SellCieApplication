package com.marlena.martins.sellcieapplication.data.payment

import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.domain.model.PaymentRequest
import com.marlena.martins.sellcieapplication.domain.model.PurchaseAttempt
import com.marlena.martins.sellcieapplication.domain.model.PurchaseAttemptStatus
import com.marlena.martins.sellcieapplication.domain.model.toAttemptStatus
import com.marlena.martins.sellcieapplication.domain.repository.PurchaseAttemptRepository
import com.marlena.martins.sellcieapplication.domain.repository.StartProcessingResult

/**
 * Implementação simples para testes e para manter o domínio independente de Room.
 */
class InMemoryPurchaseAttemptRepository : PurchaseAttemptRepository {

    private val attempts = mutableMapOf<String, PurchaseAttempt>()

    override suspend fun startProcessing(request: PaymentRequest): StartProcessingResult {
        val existing = attempts[request.purchaseId]
        when {
            existing == null -> {
                val started = PurchaseAttempt(
                    purchaseId = request.purchaseId,
                    eventId = request.eventId,
                    quantity = request.quantity,
                    items = request.items,
                    totalInCents = request.totalInCents,
                    createdAt = System.currentTimeMillis(),
                    status = PurchaseAttemptStatus.PROCESSING
                )
                attempts[request.purchaseId] = started
                return StartProcessingResult.Started(started)
            }

            existing.status == PurchaseAttemptStatus.PROCESSING -> {
                return StartProcessingResult.AlreadyProcessing
            }

            existing.outcome != null -> {
                return StartProcessingResult.AlreadyCompleted(existing.outcome)
            }

            else -> error("Tentativa em estado inválido: ${existing.status}")
        }
    }

    override suspend fun complete(purchaseId: String, outcome: PaymentOutcome): PurchaseAttempt {
        val attempt = requireNotNull(attempts[purchaseId]) { "Tentativa não encontrada." }
        check(attempt.status == PurchaseAttemptStatus.PROCESSING) {
            "A tentativa precisa estar em processamento para ser concluída."
        }
        return attempt.copy(status = outcome.toAttemptStatus(), outcome = outcome)
            .also { attempts[purchaseId] = it }
    }

    override suspend fun get(purchaseId: String): PurchaseAttempt? = attempts[purchaseId]
}
