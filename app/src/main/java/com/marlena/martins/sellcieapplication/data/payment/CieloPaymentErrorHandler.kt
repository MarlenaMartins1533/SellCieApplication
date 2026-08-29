package com.marlena.martins.sellcieapplication.data.payment

import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome

/** Centraliza o mapeamento técnico da Cielo para resultados do domínio e mensagens seguras. */
class CieloPaymentErrorHandler {
    fun outcome(responseCode: Int?, cieloCode: Int?): PaymentOutcome = when {
        // Sucesso: responseCode 0 e sem código de erro explícito (recebeu objeto Order)
        responseCode == 0 && cieloCode == null -> PaymentOutcome.Approved
        
        // Simulação de Sucesso via código (opcional, dependendo do emulador)
        cieloCode == 0 -> PaymentOutcome.Approved
        
        // Cancelamentos (78 é o Magic Value, 1 é o padrão do Deep Link)
        cieloCode == 78 || cieloCode == 1 -> PaymentOutcome.Canceled
        
        // Declinados (Magic Values)
        cieloCode == 51 || cieloCode == 54 || cieloCode == 5 || cieloCode == 25 -> PaymentOutcome.Declined
        
        // Erros Técnicos (Magic Values)
        cieloCode == 98 || cieloCode == 99 -> PaymentOutcome.TechnicalError
        
        else -> PaymentOutcome.TechnicalError
    }

    fun userMessage(outcome: PaymentOutcome, cieloCode: Int? = null): String = when (outcome) {
        PaymentOutcome.Approved -> "Pagamento aprovado com sucesso!"
        PaymentOutcome.Canceled -> "Pagamento cancelado pelo usuário ou cartão bloqueado."
        PaymentOutcome.Declined -> when (cieloCode) {
            51 -> "Saldo insuficiente no cartão."
            54 -> "Cartão com data de validade vencida."
            5 -> "Transação não autorizada pela operadora."
            else -> "O cartão foi recusado. Verifique os dados ou use outro cartão."
        }
        PaymentOutcome.TechnicalError -> when (cieloCode) {
            98 -> "Tempo limite da transação esgotado. Tente novamente."
            99 -> "Erro interno no sistema da Cielo. Tente novamente mais tarde."
            else -> "Não foi possível concluir o pagamento por problemas técnicos."
        }
        PaymentOutcome.Pending -> "O pagamento está em processamento."
    }
}
