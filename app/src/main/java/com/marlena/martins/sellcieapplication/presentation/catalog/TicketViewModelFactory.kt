package com.marlena.martins.sellcieapplication.presentation.catalog

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.marlena.martins.sellcieapplication.data.payment.LocalPaymentGateway
import com.marlena.martins.sellcieapplication.data.payment.LocalPurchaseAttemptRepository
import com.marlena.martins.sellcieapplication.data.events.LocalEventRepository
import com.marlena.martins.sellcieapplication.domain.usecase.CalculateOrderTotal
import com.marlena.martins.sellcieapplication.domain.usecase.ProcessPayment
import com.marlena.martins.sellcieapplication.domain.usecase.GetPurchaseReceipt

class TicketViewModelFactory(
    private val applicationContext: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(TicketViewModel::class.java))
        val purchaseRepository = LocalPurchaseAttemptRepository(applicationContext)
        val eventRepository = LocalEventRepository(applicationContext)
        return TicketViewModel(
            eventRepository = eventRepository,
            calculateOrderTotal = CalculateOrderTotal(),
            processPayment = ProcessPayment(
                purchaseAttemptRepository = purchaseRepository,
                paymentGateway = LocalPaymentGateway()
            ),
            getPurchaseReceipt = GetPurchaseReceipt(purchaseRepository, eventRepository)
        ) as T
    }
}
