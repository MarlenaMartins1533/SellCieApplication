package com.marlena.martins.sellcieapplication.domain.usecase

import com.marlena.martins.sellcieapplication.domain.model.Event
import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.domain.repository.EventRepository
import com.marlena.martins.sellcieapplication.domain.repository.PurchaseAttemptRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PurchaseReceipt(
    val purchaseId: String,
    val event: Event,
    val quantity: Int,
    val totalInCents: Long,
    val outcome: PaymentOutcome,
    val createdAt: Long
) {
    val shortReference: String get() = purchaseId.takeLast(8).uppercase()
    val formattedDate: String get() = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR")).format(Date(createdAt))
}

class GetPurchaseReceipt(
    private val purchaseAttemptRepository: PurchaseAttemptRepository,
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(purchaseId: String): PurchaseReceipt? {
        val attempt = purchaseAttemptRepository.get(purchaseId) ?: return null
        val outcome = attempt.outcome ?: return null
        val event = eventRepository.getEvents().firstOrNull { it.id == attempt.eventId } ?: return null
        return PurchaseReceipt(
            purchaseId = attempt.purchaseId,
            event = event,
            quantity = attempt.quantity,
            totalInCents = attempt.totalInCents,
            outcome = outcome,
            createdAt = attempt.createdAt
        )
    }
}
