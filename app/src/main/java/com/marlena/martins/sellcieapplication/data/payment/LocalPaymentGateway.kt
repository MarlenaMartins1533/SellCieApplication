package com.marlena.martins.sellcieapplication.data.payment

import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.domain.model.PaymentRequest
import com.marlena.martins.sellcieapplication.domain.model.PaymentSimulation
import com.marlena.martins.sellcieapplication.domain.repository.PaymentGateway
import kotlinx.coroutines.delay

/** Gateway determinístico e exclusivamente local para validar a T2 offline. */
class LocalPaymentGateway : PaymentGateway {

    override suspend fun process(request: PaymentRequest): PaymentOutcome {
        delay(400)
        return when (request.simulation) {
            PaymentSimulation.APPROVED -> PaymentOutcome.Approved
            PaymentSimulation.DECLINED -> PaymentOutcome.Declined
            PaymentSimulation.CANCELED -> PaymentOutcome.Canceled
            PaymentSimulation.TECHNICAL_ERROR -> PaymentOutcome.TechnicalError
        }
    }
}
