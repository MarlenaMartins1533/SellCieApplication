package com.marlena.martins.sellcieapplication

import android.app.Activity
import android.os.Bundle
import com.marlena.martins.sellcieapplication.data.payment.CieloPaymentGatewayRegistry
import com.marlena.martins.sellcieapplication.data.payment.PaymentForegroundService

class PaymentResponseActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CieloPaymentGatewayRegistry.get(applicationContext).completeFromCallback(intent?.data)
        PaymentForegroundService.stop(applicationContext)
        finish()
    }
}
