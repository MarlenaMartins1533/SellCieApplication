package com.marlena.martins.sellcieapplication.data.payment

import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import org.junit.Assert.assertEquals
import org.junit.Test

class CieloPaymentErrorHandlerTest {
    private val handler = CieloPaymentErrorHandler()

    @Test fun `response code zero approves`() = assertEquals(PaymentOutcome.Approved, handler.outcome(0, null))
    @Test fun `cielo code one cancels`() = assertEquals(PaymentOutcome.Canceled, handler.outcome(2, 1))
    @Test fun `payment and authentication errors are technical`() {
        assertEquals(PaymentOutcome.TechnicalError, handler.outcome(2, 3))
        assertEquals(PaymentOutcome.TechnicalError, handler.outcome(2, 4))
    }
}
