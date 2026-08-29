package com.marlena.martins.sellcieapplication.domain.usecase

import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.domain.model.PurchasedTicket
import com.marlena.martins.sellcieapplication.domain.repository.EventRepository
import com.marlena.martins.sellcieapplication.domain.repository.PurchaseAttemptRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PurchaseReceipt(
    val purchaseId: String,
    val items: List<PurchasedTicket>,
    val totalInCents: Long,
    val outcome: PaymentOutcome,
    val createdAt: Long,
    val cieloMetadata: Map<String, String>? = null
) {
    val shortReference: String get() = purchaseId.takeLast(8).uppercase()
    val formattedDate: String get() = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR")).format(Date(createdAt))
    val totalQuantity: Int get() = items.sumOf(PurchasedTicket::quantity)
}

class GetPurchaseReceipt(
    private val purchaseAttemptRepository: PurchaseAttemptRepository,
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(purchaseId: String): PurchaseReceipt? {
        val attempt = purchaseAttemptRepository.get(purchaseId) ?: return null
        val outcome = attempt.outcome ?: return null
        val items = attempt.items.ifEmpty {
            legacyItem(attempt.eventId, attempt.quantity, attempt.totalInCents) ?: return null
        }
        return PurchaseReceipt(
            purchaseId = attempt.purchaseId,
            items = items,
            totalInCents = attempt.totalInCents,
            outcome = outcome,
            createdAt = attempt.createdAt,
            cieloMetadata = attempt.cieloMetadata
        )
    }

    private fun legacyItem(eventId: String, quantity: Int, totalInCents: Long): List<PurchasedTicket>? {
        val event = eventRepository.getEvents().firstOrNull { it.id == eventId } ?: return null
        val unitPrice = if (quantity > 0) totalInCents / quantity else event.priceInCents
        return listOf(
            PurchasedTicket(
                eventId = event.id,
                title = event.title,
                quantity = quantity,
                unitPriceInCents = unitPrice
            )
        )
    }
}
