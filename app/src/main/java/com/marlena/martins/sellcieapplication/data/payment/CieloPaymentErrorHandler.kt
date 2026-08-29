package com.marlena.martins.sellcieapplication.data.payment

import androidx.annotation.StringRes
import com.marlena.martins.sellcieapplication.R
import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome

/** Centraliza o mapeamento técnico da Cielo para resultados do domínio e mensagens seguras. */
class CieloPaymentErrorHandler {
    
    fun outcome(responseCode: Int?, cieloCode: Int?): PaymentOutcome = when {
        // Sucesso: responseCode 0 e sem código de erro explícito (recebeu objeto Order)
        responseCode == CieloConstants.CODE_SUCCESS && cieloCode == null -> PaymentOutcome.Approved
        
        // Simulação de Sucesso via código
        cieloCode == CieloConstants.CODE_SUCCESS -> PaymentOutcome.Approved
        
        // Cancelamentos
        cieloCode == CieloConstants.CODE_CANCELED || cieloCode == CieloConstants.CODE_USER_CANCELED -> 
            PaymentOutcome.Canceled
        
        // Declinados
        cieloCode == CieloConstants.CODE_INSUFFICIENT_FUNDS || 
        cieloCode == CieloConstants.CODE_EXPIRED_CARD || 
        cieloCode == CieloConstants.CODE_NOT_AUTHORIZED || 
        cieloCode == CieloConstants.CODE_DECLINED -> 
            PaymentOutcome.Declined
        
        // Erros Técnicos
        cieloCode == CieloConstants.CODE_TIMEOUT || cieloCode == CieloConstants.CODE_SYSTEM_ERROR -> 
            PaymentOutcome.TechnicalError
        
        else -> PaymentOutcome.TechnicalError
    }

    @StringRes
    fun userMessage(outcome: PaymentOutcome, cieloCode: Int? = null): Int = when (outcome) {
        PaymentOutcome.Approved -> R.string.cielo_outcome_approved
        PaymentOutcome.Canceled -> R.string.cielo_outcome_canceled
        PaymentOutcome.Declined -> when (cieloCode) {
            CieloConstants.CODE_INSUFFICIENT_FUNDS -> R.string.cielo_outcome_insufficient_funds
            CieloConstants.CODE_EXPIRED_CARD -> R.string.cielo_outcome_expired_card
            CieloConstants.CODE_NOT_AUTHORIZED -> R.string.cielo_outcome_not_authorized
            else -> R.string.cielo_outcome_declined
        }
        PaymentOutcome.TechnicalError -> when (cieloCode) {
            CieloConstants.CODE_TIMEOUT -> R.string.cielo_outcome_timeout
            CieloConstants.CODE_SYSTEM_ERROR -> R.string.cielo_outcome_system_error
            else -> R.string.cielo_outcome_technical_error
        }
        PaymentOutcome.Pending -> R.string.cielo_outcome_pending
    }
}
