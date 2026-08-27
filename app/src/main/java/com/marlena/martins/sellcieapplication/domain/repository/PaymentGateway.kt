package com.marlena.martins.sellcieapplication.domain.repository

import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.domain.model.PaymentRequest

interface PaymentGateway {
    suspend fun process(request: PaymentRequest): PaymentOutcome
}
