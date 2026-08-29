package com.marlena.martins.sellcieapplication.data.payment

import com.marlena.martins.sellcieapplication.domain.model.PaymentRequest
import com.marlena.martins.sellcieapplication.domain.model.PurchasedTicket
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.nio.charset.StandardCharsets
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
class CieloPaymentContractTest {
    @Test
    fun `payload uses cents and preserves all items`() {
        val request = PaymentRequest("purchase-1", 2500, items = listOf(PurchasedTicket("event-1", "Ingresso", 2, 1250)))
        val json = String(Base64.Default.decode(
            CieloPaymentContract.encodedRequest(request, "client", "token")
        ), StandardCharsets.UTF_8)

        assert(json.contains("\"reference\":\"purchase-1\""))
        assert(json.contains("\"value\":\"2500\""))
        assert(json.contains("\"unitPrice\":1250"))
        assert(json.contains("\"quantity\":2"))
    }

    @Test
    fun `callback validates and decodes response`() {
        val response = Base64.Default.encode("{\"code\":1,\"reason\":\"cancelado\"}".toByteArray())
        val callback = CieloPaymentContract.parseCallback("sellcie://payment-result?response=$response&responsecode=2")
        assertEquals(2, callback.responseCode)
        assertEquals(1, callback.cieloCode)
    }

    @Test
    fun `callback rejects wrong contract`() {
        assertThrows(IllegalArgumentException::class.java) {
            CieloPaymentContract.parseCallback("other://payment-result?response=x&responsecode=0")
        }
    }
}
