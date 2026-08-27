package com.marlena.martins.sellcieapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.marlena.martins.sellcieapplication.presentation.catalog.TicketCatalogRoute
import com.marlena.martins.sellcieapplication.presentation.catalog.TicketViewModel
import com.marlena.martins.sellcieapplication.presentation.catalog.TicketViewModelFactory
import com.marlena.martins.sellcieapplication.presentation.theme.CieloTicketTheme

class FirstFragment : Fragment() {

    private val viewModel: TicketViewModel by viewModels {
        TicketViewModelFactory(requireContext().applicationContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            CieloTicketTheme {
                TicketCatalogRoute(viewModel = viewModel)
            }
        }
    }
}
