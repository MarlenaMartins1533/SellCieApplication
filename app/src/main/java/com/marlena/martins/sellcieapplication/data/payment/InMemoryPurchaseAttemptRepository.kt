package com.marlena.martins.sellcieapplication.data.payment

import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.domain.model.PaymentRequest
import com.marlena.martins.sellcieapplication.domain.model.PurchaseAttempt
import com.marlena.martins.sellcieapplication.domain.model.PurchaseAttemptStatus
import com.marlena.martins.sellcieapplication.domain.model.toAttemptStatus
import com.marlena.martins.sellcieapplication.domain.repository.PurchaseAttemptRepository
import com.marlena.martins.sellcieapplication.domain.repository.StartProcessingResult

/**
 * Armazena tentativas enquanto o app está aberto. A persistência em banco local
 * será adicionada na T3 sem alterar o contrato usado pelo caso de uso.
 */
class InMemoryPurchaseAttemptRepository : PurchaseAttemptRepository {

    private val attempts = mutableMapOf<String, PurchaseAttempt>()

    @Synchronized
    override fun startProcessing(request: PaymentRequest): StartProcessingResult {
        val existing = attempts[request.purchaseId]
        when {
            existing == null -> {
                val started = PurchaseAttempt(
                    purchaseId = request.purchaseId,
                    totalInCents = request.totalInCents,
                    simulation = request.simulation,
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

    @Synchronized
    override fun complete(purchaseId: String, outcome: PaymentOutcome): PurchaseAttempt {
        val attempt = requireNotNull(attempts[purchaseId]) { "Tentativa não encontrada." }
        check(attempt.status == PurchaseAttemptStatus.PROCESSING) {
            "A tentativa precisa estar em processamento para ser concluída."
        }
        return attempt.copy(status = outcome.toAttemptStatus(), outcome = outcome)
            .also { attempts[purchaseId] = it }
    }
}
