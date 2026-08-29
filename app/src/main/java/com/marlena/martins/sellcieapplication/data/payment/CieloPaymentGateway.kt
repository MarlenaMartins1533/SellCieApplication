package com.marlena.martins.sellcieapplication.data.payment

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import com.marlena.martins.sellcieapplication.BuildConfig
import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.domain.model.PaymentRequest
import com.marlena.martins.sellcieapplication.domain.repository.PaymentGateway
import com.marlena.martins.sellcieapplication.domain.repository.PaymentGatewayResult
import kotlinx.coroutines.CompletableDeferred

class CieloPaymentGateway(private val context: Context) : PaymentGateway {
    private val pending = mutableMapOf<String, CompletableDeferred<PaymentGatewayResult>>()
    private val errorHandler = CieloPaymentErrorHandler()

    override suspend fun process(request: PaymentRequest): PaymentGatewayResult {
        // Removemos a dependência estrita do flag de emulador para as credenciais dummy,
        // garantindo que o Deep Link sempre seja disparado para o "app sample" (emulador).
        val clientId = BuildConfig.CIELO_CLIENT_ID.ifBlank { "EMULATOR_CLIENT_ID" }
        val accessToken = BuildConfig.CIELO_ACCESS_TOKEN.ifBlank { "EMULATOR_ACCESS_TOKEN" }

        val result = CompletableDeferred<PaymentGatewayResult>()
        synchronized(pending) { pending[request.purchaseId] = result }
        try {
            val intent = Intent(Intent.ACTION_VIEW, CieloPaymentContract.paymentUri(
                request, clientId, accessToken
            )).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            PaymentForegroundService.start(context)
            context.startActivity(intent)
            return result.await()
        } catch (_: ActivityNotFoundException) {
            return PaymentGatewayResult(PaymentOutcome.TechnicalError)
        } catch (_: SecurityException) {
            return PaymentGatewayResult(PaymentOutcome.TechnicalError)
        } finally {
            synchronized(pending) { pending.remove(request.purchaseId) }
            PaymentForegroundService.stop(context)
        }
    }

    fun completeFromCallback(uri: android.net.Uri?): Boolean {
        val callback = runCatching { CieloPaymentContract.parseCallback(uri) }.getOrElse {
            completeAny(PaymentGatewayResult(PaymentOutcome.TechnicalError))
            return false
        }
        val outcome = errorHandler.outcome(callback.responseCode, callback.cieloCode)
        return completeAny(PaymentGatewayResult(outcome, callback.metadata))
    }

    private fun completeAny(result: PaymentGatewayResult): Boolean {
        val deferred = synchronized(pending) { pending.values.firstOrNull() } ?: return false
        return deferred.complete(result)
    }
}

object CieloPaymentGatewayRegistry {
    @Volatile private var instance: CieloPaymentGateway? = null
    fun get(context: Context): CieloPaymentGateway = instance ?: synchronized(this) {
        instance ?: CieloPaymentGateway(context.applicationContext).also { instance = it }
    }
}
