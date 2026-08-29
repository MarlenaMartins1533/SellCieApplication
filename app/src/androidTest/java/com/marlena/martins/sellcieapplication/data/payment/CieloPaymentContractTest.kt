package com.marlena.martins.sellcieapplication.data.payment

import com.marlena.martins.sellcieapplication.domain.model.PaymentRequest
import com.marlena.martins.sellcieapplication.domain.model.PurchasedTicket
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.nio.charset.StandardCharsets
import android.util.Base64

class CieloPaymentContractTest {
    @Test
    fun payloadPreservesAllRequiredFieldsAndEncodedInBase64() {
        val request = PaymentRequest("purchase-1", 2500, items = listOf(PurchasedTicket("event-1", "Ingresso", 2, 1250)))
        val encoded = CieloPaymentContract.encodedRequest(request, "client", "token")
        val json = String(Base64.decode(encoded, Base64.DEFAULT), StandardCharsets.UTF_8)

        assert(json.contains("\"reference\":\"purchase-1\""))
        assert(json.contains("\"value\":2500"))
        assert(json.contains("\"unitPrice\":1250"))
        assert(json.contains("\"quantity\":2"))
        assert(json.contains("\"clientID\":\"client\""))
    }

    @Test
    fun callbackParsesSuccessfulOrderAndExtractsMetadata() {
        val responseJson = """
            {
              "id": "ord-1",
              "payments": [{
                "cieloCode": "123456",
                "authCode": "987654",
                "brand": "Visa"
              }]
            }
        """.trimIndent()
        val responseEncoded = Base64.encodeToString(responseJson.toByteArray(), Base64.NO_WRAP)
        val callback = CieloPaymentContract.parseCallback(
            android.net.Uri.parse("${CieloConstants.CALLBACK_URI}?${CieloConstants.PARAM_RESPONSE}=$responseEncoded&${CieloConstants.PARAM_RESPONSE_CODE}=0")
        )
        
        assertEquals(0, callback.responseCode)
        assertEquals(null, callback.cieloCode)
        assertEquals("123456", callback.metadata[CieloConstants.KEY_CIELO_CODE])
        assertEquals("Visa", callback.metadata[CieloConstants.KEY_BRAND])
    }

    @Test
    fun callbackParsesErrorResponse() {
        val responseJson = "{\"code\":51,\"reason\":\"Saldo insuficiente\"}"
        val responseEncoded = Base64.encodeToString(responseJson.toByteArray(), Base64.NO_WRAP)
        val callback = CieloPaymentContract.parseCallback(
            android.net.Uri.parse("${CieloConstants.CALLBACK_URI}?${CieloConstants.PARAM_RESPONSE}=$responseEncoded&${CieloConstants.PARAM_RESPONSE_CODE}=2")
        )
        
        assertEquals(2, callback.responseCode)
        assertEquals(51, callback.cieloCode)
        assertEquals("Saldo insuficiente", callback.metadata[CieloConstants.KEY_REASON])
    }

    @Test
    fun callbackThrowsOnWrongScheme() {
        assertThrows(IllegalArgumentException::class.java) {
            CieloPaymentContract.parseCallback(android.net.Uri.parse("other://payment-result?response=x&responsecode=0"))
        }
    }
}
