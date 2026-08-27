package com.marlena.martins.sellcieapplication.data.payment

import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.domain.model.PaymentRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class CieloLioPaymentGatewayTest {

    @Test
    fun `maps every Cielo result to the app outcome`() = runBlocking {
        val expectedMappings = mapOf(
            CieloLioResult.Approved to PaymentOutcome.Approved,
            CieloLioResult.Declined to PaymentOutcome.Declined,
            CieloLioResult.Canceled to PaymentOutcome.Canceled,
            CieloLioResult.TechnicalError to PaymentOutcome.TechnicalError
        )

        expectedMappings.forEach { (cieloResult, expectedOutcome) ->
            val gateway = CieloLioPaymentGateway(FakeCieloLioClient(cieloResult))

            assertEquals(expectedOutcome, gateway.process(PaymentRequest("purchase-$cieloResult", 2500)))
        }
    }

    private class FakeCieloLioClient(
        private val result: CieloLioResult
    ) : CieloLioClient {
        override suspend fun process(totalInCents: Long): CieloLioResult = result
    }
}
