package com.marlena.martins.sellcieapplication.data.payment

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import com.marlena.martins.sellcieapplication.BuildConfig
import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.domain.model.PaymentRequest
import com.marlena.martins.sellcieapplication.domain.repository.PaymentGateway
import kotlinx.coroutines.CompletableDeferred

class CieloPaymentGateway(private val context: Context) : PaymentGateway {
    private val pending = mutableMapOf<String, CompletableDeferred<PaymentOutcome>>()
    private val errorHandler = CieloPaymentErrorHandler()

    override suspend fun process(request: PaymentRequest): PaymentOutcome {
        if (BuildConfig.CIELO_CLIENT_ID.isBlank() || BuildConfig.CIELO_ACCESS_TOKEN.isBlank()) {
            return PaymentOutcome.TechnicalError
        }
        val result = CompletableDeferred<PaymentOutcome>()
        synchronized(pending) { pending[request.purchaseId] = result }
        try {
            val intent = Intent(Intent.ACTION_VIEW, CieloPaymentContract.paymentUri(
                request, BuildConfig.CIELO_CLIENT_ID, BuildConfig.CIELO_ACCESS_TOKEN
            )).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            PaymentForegroundService.start(context)
            context.startActivity(intent)
            return result.await()
        } catch (_: ActivityNotFoundException) {
            return PaymentOutcome.TechnicalError
        } catch (_: SecurityException) {
            return PaymentOutcome.TechnicalError
        } finally {
            synchronized(pending) { pending.remove(request.purchaseId) }
            PaymentForegroundService.stop(context)
        }
    }

    fun completeFromCallback(uri: android.net.Uri?): Boolean {
        val callback = runCatching { CieloPaymentContract.parseCallback(uri) }.getOrElse {
            completeAny(PaymentOutcome.TechnicalError)
            return false
        }
        return completeAny(errorHandler.outcome(callback.responseCode, callback.cieloCode))
    }

    private fun completeAny(outcome: PaymentOutcome): Boolean {
        val deferred = synchronized(pending) { pending.values.firstOrNull() } ?: return false
        return deferred.complete(outcome)
    }
}

object CieloPaymentGatewayRegistry {
    @Volatile private var instance: CieloPaymentGateway? = null
    fun get(context: Context): CieloPaymentGateway = instance ?: synchronized(this) {
        instance ?: CieloPaymentGateway(context.applicationContext).also { instance = it }
    }
}
