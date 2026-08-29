package com.marlena.martins.sellcieapplication.data.payment

import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.domain.model.PaymentRequest
import com.marlena.martins.sellcieapplication.domain.repository.PaymentGateway
import com.marlena.martins.sellcieapplication.domain.repository.PaymentGatewayResult

/** Gateway offline, determinístico e substituível pelo adaptador Cielo validado localmente. */
class LocalPaymentGateway(
    private val outcome: PaymentOutcome = PaymentOutcome.Approved
) : PaymentGateway {
    override suspend fun process(request: PaymentRequest): PaymentGatewayResult = PaymentGatewayResult(outcome)
}
