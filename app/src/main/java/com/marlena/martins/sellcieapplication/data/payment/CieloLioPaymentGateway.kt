package com.marlena.martins.sellcieapplication.data.payment

import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.domain.model.PaymentRequest
import com.marlena.martins.sellcieapplication.domain.repository.PaymentGateway

/**
 * Adaptador para uma futura integração local com Cielo Lio. O SDK não é
 * referenciado enquanto sua versão e seu contrato não forem confirmados.
 */
class CieloLioPaymentGateway(
    private val client: CieloLioClient
) : PaymentGateway {

    override suspend fun process(request: PaymentRequest): PaymentOutcome = when (
        client.process(totalInCents = request.totalInCents)
    ) {
        CieloLioResult.Approved -> PaymentOutcome.Approved
        CieloLioResult.Declined -> PaymentOutcome.Declined
        CieloLioResult.Canceled -> PaymentOutcome.Canceled
        CieloLioResult.TechnicalError -> PaymentOutcome.TechnicalError
    }
}

interface CieloLioClient {
    suspend fun process(totalInCents: Long): CieloLioResult
}

sealed interface CieloLioResult {
    data object Approved : CieloLioResult
    data object Declined : CieloLioResult
    data object Canceled : CieloLioResult
    data object TechnicalError : CieloLioResult
}
