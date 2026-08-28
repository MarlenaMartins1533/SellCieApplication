package com.marlena.martins.sellcieapplication.domain.model

data class PaymentRequest(
    val purchaseId: String,
    val totalInCents: Long,
    val eventId: String = "",
    val quantity: Int = 0,
    val items: List<PurchasedTicket> = emptyList()
) {
    init {
        require(purchaseId.isNotBlank()) { "A tentativa precisa de um identificador." }
        require(totalInCents > 0) { "O total deve ser maior que zero." }
    }
}

data class PurchasedTicket(
    val eventId: String,
    val title: String,
    val quantity: Int,
    val unitPriceInCents: Long
) {
    init {
        require(eventId.isNotBlank()) { "O item precisa de um evento." }
        require(title.isNotBlank()) { "O item precisa de um nome." }
        require(quantity > 0) { "A quantidade do item deve ser maior que zero." }
        require(unitPriceInCents >= 0) { "O preço do item não pode ser negativo." }
    }

    val subtotalInCents: Long get() = quantity * unitPriceInCents
}

sealed interface PaymentOutcome {
    data object Approved : PaymentOutcome
    data object Pending : PaymentOutcome
    data object Declined : PaymentOutcome
    data object Canceled : PaymentOutcome
    data object TechnicalError : PaymentOutcome
}

enum class PurchaseAttemptStatus {
    CREATED,
    PROCESSING,
    APPROVED,
    PENDING,
    DECLINED,
    CANCELED,
    TECHNICAL_ERROR
}

data class PurchaseAttempt(
    val purchaseId: String,
    val totalInCents: Long,
    val eventId: String = "",
    val quantity: Int = 0,
    val items: List<PurchasedTicket> = emptyList(),
    val createdAt: Long = 0L,
    val status: PurchaseAttemptStatus = PurchaseAttemptStatus.CREATED,
    val outcome: PaymentOutcome? = null
)

fun PaymentOutcome.toAttemptStatus(): PurchaseAttemptStatus = when (this) {
    PaymentOutcome.Approved -> PurchaseAttemptStatus.APPROVED
    PaymentOutcome.Pending -> PurchaseAttemptStatus.PENDING
    PaymentOutcome.Declined -> PurchaseAttemptStatus.DECLINED
    PaymentOutcome.Canceled -> PurchaseAttemptStatus.CANCELED
    PaymentOutcome.TechnicalError -> PurchaseAttemptStatus.TECHNICAL_ERROR
}
