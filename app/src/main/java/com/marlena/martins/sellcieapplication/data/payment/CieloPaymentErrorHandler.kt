package com.marlena.martins.sellcieapplication.data.payment

import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome

/** Centraliza o mapeamento técnico da Cielo para resultados do domínio e mensagens seguras. */
class CieloPaymentErrorHandler {
    fun outcome(responseCode: Int?, cieloCode: Int?): PaymentOutcome = when {
        responseCode == 0 -> PaymentOutcome.Approved
        cieloCode == 1 -> PaymentOutcome.Canceled
        else -> PaymentOutcome.TechnicalError
    }

    fun userMessage(outcome: PaymentOutcome): String = when (outcome) {
        PaymentOutcome.Canceled -> "Pagamento cancelado. Você pode tentar novamente."
        PaymentOutcome.TechnicalError -> "Não foi possível concluir o pagamento. Tente novamente."
        else -> ""
    }
}
