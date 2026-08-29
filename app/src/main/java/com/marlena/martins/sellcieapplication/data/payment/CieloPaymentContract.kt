package com.marlena.martins.sellcieapplication.data.payment

import android.net.Uri
import com.marlena.martins.sellcieapplication.domain.model.PaymentRequest
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
object CieloPaymentContract {
    const val scheme = "sellcie"
    const val host = "payment-result"
    const val callbackUri = "$scheme://$host"
    private const val paymentUri = "lio://payment"

    fun encodedRequest(request: PaymentRequest, clientId: String, accessToken: String): String {
        val items = request.items.joinToString(",", prefix = "[", postfix = "]") { item ->
            "{" +
                "\"name\":\"${item.title.jsonEscape()}\"," +
                "\"sku\":\"${item.eventId.jsonEscape()}\"," +
                "\"quantity\":${item.quantity}," +
                "\"unitOfMeasure\":\"unidade\"," +
                "\"unitPrice\":${item.unitPriceInCents}" +
                "}"
        }
        val payload = "{" +
            "\"clientID\":\"${clientId.jsonEscape()}\"," +
            "\"accessToken\":\"${accessToken.jsonEscape()}\"," +
            "\"reference\":\"${request.purchaseId.jsonEscape()}\"," +
            "\"installments\":0," +
            "\"paymentCode\":\"DEBITO_AVISTA\"," +
            "\"value\":\"${request.totalInCents}\"," +
            "\"items\":$items" +
            "}"
        return Base64.Default.encode(payload.toByteArray(StandardCharsets.UTF_8))
    }

    fun paymentUri(request: PaymentRequest, clientId: String, accessToken: String): Uri =
        Uri.parse(paymentUri)
            .buildUpon()
            .appendQueryParameter("request", encodedRequest(request, clientId, accessToken))
            .appendQueryParameter("urlCallback", callbackUri)
            .build()

    fun parseCallback(uri: Uri?): CieloCallback {
        return parseCallback(uri?.toString())
    }

    fun parseCallback(rawUri: String?): CieloCallback {
        val parsedUri = runCatching { URI(requireNotNull(rawUri)) }.getOrNull()
        require(parsedUri?.scheme == scheme && parsedUri.host == host) { "Callback inválido." }
        val query = parsedUri.rawQuery.orEmpty().split('&').mapNotNull { part ->
            val separator = part.indexOf('=')
            if (separator <= 0) null else part.substring(0, separator) to
                URLDecoder.decode(part.substring(separator + 1), StandardCharsets.UTF_8.name())
        }.toMap()
        val encodedResponse = requireNotNull(query["response"]) { "Resposta ausente." }
        require(encodedResponse.isNotBlank()) { "Resposta vazia." }
        val responseJson = try {
            val decoded = Base64.Default.decode(encodedResponse).toString(StandardCharsets.UTF_8)
            require(decoded.isJsonObject()) { "JSON inválido." }
            decoded
        } catch (error: Exception) {
            throw IllegalArgumentException("Resposta inválida.", error)
        }
        val responseCode = query["responsecode"]?.toIntOrNull()
            ?: throw IllegalArgumentException("Código de resposta ausente.")
        val cieloCode = Regex("\\\"code\\\"\\s*:\\s*(-?\\d+)").find(responseJson)
            ?.groupValues?.get(1)?.toIntOrNull()
        return CieloCallback(responseCode, cieloCode)
    }

    private fun String.jsonEscape(): String = buildString {
        for (character in this@jsonEscape) when (character) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> append(character)
        }
    }

    private fun String.isJsonObject(): Boolean {
        val value = trim()
        if (value.length < 2 || value.first() != '{' || value.last() != '}') return false
        var depth = 0
        var escaped = false
        var quoted = false
        value.forEachIndexed { index, character ->
            if (escaped) {
                escaped = false
                return@forEachIndexed
            }
            if (character == '\\' && quoted) {
                escaped = true
                return@forEachIndexed
            }
            if (character == '"') quoted = !quoted
            if (!quoted) when (character) {
                '{' -> depth++
                '}' -> depth--
            }
            if (depth < 0 || (index == value.lastIndex && (quoted || depth != 0))) return false
        }
        return !quoted && depth == 0
    }
}

data class CieloCallback(val responseCode: Int, val cieloCode: Int?)
