package com.marlena.martins.sellcieapplication.data.payment

import android.net.Uri
import android.util.Base64
import com.google.gson.Gson
import com.marlena.martins.sellcieapplication.data.payment.model.Item
import com.marlena.martins.sellcieapplication.data.payment.model.Order
import com.marlena.martins.sellcieapplication.data.payment.model.OrderRequest
import com.marlena.martins.sellcieapplication.domain.model.PaymentRequest
import java.nio.charset.StandardCharsets

object CieloPaymentContract {
    private const val paymentUri = CieloConstants.PAYMENT_URI

    private val gson = Gson()

    fun encodedRequest(request: PaymentRequest, clientId: String, accessToken: String): String {
        val items = request.items.map { item ->
            Item(
                sku = item.eventId,
                name = item.title,
                unitPrice = item.unitPriceInCents,
                quantity = item.quantity,
                unitOfMeasure = CieloConstants.DEFAULT_UNIT_OF_MEASURE
            )
        }.toMutableList()

        val orderRequest = OrderRequest(
            clientID = clientId,
            accessToken = accessToken,
            value = request.totalInCents,
            paymentCode = CieloConstants.DEFAULT_PAYMENT_CODE,
            installments = CieloConstants.DEFAULT_INSTALLMENTS,
            email = CieloConstants.DEFAULT_EMAIL,
            merchantCode = null,
            reference = request.purchaseId,
            items = items
        )

        val json = gson.toJson(orderRequest)
        return Base64.encodeToString(json.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
    }

    fun paymentUri(request: PaymentRequest, clientId: String, accessToken: String): Uri =
        Uri.parse(paymentUri)
            .buildUpon()
            .appendQueryParameter("request", encodedRequest(request, clientId, accessToken))
            .appendQueryParameter("urlCallback", CieloConstants.CALLBACK_URI)
            .build()

    fun parseCallback(uri: Uri?): CieloCallback {
        val encodedResponse = uri?.getQueryParameter(CieloConstants.PARAM_RESPONSE) 
            ?: throw IllegalArgumentException("Resposta ausente.")
        val responseCode = uri.getQueryParameter(CieloConstants.PARAM_RESPONSE_CODE)?.toIntOrNull()
            ?: throw IllegalArgumentException("Código de resposta ausente.")

        val responseJson = String(Base64.decode(encodedResponse, Base64.DEFAULT), StandardCharsets.UTF_8)
        
        val metadata = mutableMapOf<String, String>()
        val cieloCode = try {
            // Tenta primeiro o formato de sucesso (Order)
            val order = gson.fromJson(responseJson, Order::class.java)
            if (order?.id != null && order.payments != null && order.payments.isNotEmpty()) {
                val payment = order.payments.first()
                payment.authCode.takeIf { it.isNotBlank() }?.let { metadata[CieloConstants.KEY_AUTH_CODE] = it }
                payment.cieloCode.takeIf { it.isNotBlank() }?.let { metadata[CieloConstants.KEY_CIELO_CODE] = it }
                payment.brand.takeIf { it.isNotBlank() }?.let { metadata[CieloConstants.KEY_BRAND] = it }
                payment.mask.takeIf { it.isNotBlank() }?.let { metadata[CieloConstants.KEY_CARD_MASK] = it }
                payment.terminal.takeIf { it.isNotBlank() }?.let { metadata[CieloConstants.KEY_TERMINAL] = it }
                null 
            } else {
                // Tenta o formato de erro {"code": X, "reason": "..."}
                val error = gson.fromJson(responseJson, CieloErrorResponse::class.java)
                error?.reason?.let { metadata[CieloConstants.KEY_REASON] = it }
                error?.code
            }
        } catch (_: Exception) {
            null
        }

        return CieloCallback(responseCode, cieloCode, metadata)
    }
}

data class CieloCallback(
    val responseCode: Int,
    val cieloCode: Int?,
    val metadata: Map<String, String> = emptyMap()
)
private data class CieloErrorResponse(val code: Int?, val reason: String?)
