package com.marlena.martins.sellcieapplication.data.payment

import com.marlena.martins.sellcieapplication.R
import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import org.junit.Assert.assertEquals
import org.junit.Test

class CieloPaymentErrorHandlerTest {
    private val handler = CieloPaymentErrorHandler()

    @Test 
    fun `response code success and null cielo code approves`() {
        assertEquals(PaymentOutcome.Approved, handler.outcome(CieloConstants.CODE_SUCCESS, null))
    }
    
    @Test 
    fun `cielo magic values map to correct outcomes`() {
        assertEquals(PaymentOutcome.Approved, handler.outcome(0, CieloConstants.CODE_SUCCESS))
        assertEquals(PaymentOutcome.Declined, handler.outcome(0, CieloConstants.CODE_INSUFFICIENT_FUNDS))
        assertEquals(PaymentOutcome.Canceled, handler.outcome(0, CieloConstants.CODE_CANCELED))
        assertEquals(PaymentOutcome.TechnicalError, handler.outcome(0, CieloConstants.CODE_TIMEOUT))
    }

    @Test 
    fun `user messages return correct string resources`() {
        assertEquals(R.string.cielo_outcome_approved, handler.userMessage(PaymentOutcome.Approved))
        assertEquals(R.string.cielo_outcome_insufficient_funds, handler.userMessage(PaymentOutcome.Declined, CieloConstants.CODE_INSUFFICIENT_FUNDS))
        assertEquals(R.string.cielo_outcome_timeout, handler.userMessage(PaymentOutcome.TechnicalError, CieloConstants.CODE_TIMEOUT))
    }
}
