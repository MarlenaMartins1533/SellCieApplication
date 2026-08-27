package com.marlena.martins.sellcieapplication.presentation.catalog

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.marlena.martins.sellcieapplication.data.events.LocalEventRepository
import com.marlena.martins.sellcieapplication.domain.usecase.CalculateOrderTotal

class TicketViewModelFactory(
    private val applicationContext: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(TicketViewModel::class.java))
        return TicketViewModel(
            eventRepository = LocalEventRepository(applicationContext),
            calculateOrderTotal = CalculateOrderTotal()
        ) as T
    }
}
