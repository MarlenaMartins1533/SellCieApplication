package com.marlena.martins.sellcieapplication.domain.model

enum class PaymentSimulation {
    APPROVED,
    DECLINED,
    CANCELED,
    TECHNICAL_ERROR
}

data class PaymentRequest(
    val purchaseId: String,
    val totalInCents: Long,
    val simulation: PaymentSimulation = PaymentSimulation.APPROVED
) {
    init {
        require(purchaseId.isNotBlank()) { "A tentativa precisa de um identificador." }
        require(totalInCents > 0) { "O total deve ser maior que zero." }
    }
}

sealed interface PaymentOutcome {
    data object Approved : PaymentOutcome
    data object Declined : PaymentOutcome
    data object Canceled : PaymentOutcome
    data object TechnicalError : PaymentOutcome
}

enum class PurchaseAttemptStatus {
    CREATED,
    PROCESSING,
    APPROVED,
    DECLINED,
    CANCELED,
    TECHNICAL_ERROR
}

data class PurchaseAttempt(
    val purchaseId: String,
    val totalInCents: Long,
    val simulation: PaymentSimulation,
    val status: PurchaseAttemptStatus = PurchaseAttemptStatus.CREATED,
    val outcome: PaymentOutcome? = null
)

fun PaymentOutcome.toAttemptStatus(): PurchaseAttemptStatus = when (this) {
    PaymentOutcome.Approved -> PurchaseAttemptStatus.APPROVED
    PaymentOutcome.Declined -> PurchaseAttemptStatus.DECLINED
    PaymentOutcome.Canceled -> PurchaseAttemptStatus.CANCELED
    PaymentOutcome.TechnicalError -> PurchaseAttemptStatus.TECHNICAL_ERROR
}
