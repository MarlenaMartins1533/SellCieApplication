package com.marlena.martins.sellcieapplication.presentation.checkout

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.domain.model.PaymentSimulation
import com.marlena.martins.sellcieapplication.presentation.catalog.CatalogTestTags
import com.marlena.martins.sellcieapplication.presentation.catalog.PaymentUiState
import com.marlena.martins.sellcieapplication.presentation.theme.CieloTicketTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CheckoutScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun processingDisablesConfirmationAndBackNavigation() {
        composeRule.setContent {
            CieloTicketTheme {
                CheckoutScreen(
                    selectedTicketCount = 2,
                    totalInCents = 5000,
                    paymentState = PaymentUiState.Processing("purchase-1"),
                    selectedSimulation = PaymentSimulation.APPROVED,
                    onSimulationSelected = {},
                    onConfirm = {},
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithTag(CatalogTestTags.CHECKOUT_CONFIRM_BUTTON).assertIsNotEnabled()
        composeRule.onNodeWithTag(CatalogTestTags.CHECKOUT_BACK_BUTTON).assertIsNotEnabled()
        composeRule.onNodeWithText("Cielo Ingressos").assertExists()
        composeRule.onNodeWithText("Processando pagamento…").assertExists()
    }

    @Test
    fun technicalErrorShowsSafeGuidance() {
        composeRule.setContent {
            CieloTicketTheme {
                CheckoutScreen(
                    selectedTicketCount = 1,
                    totalInCents = 2500,
                    paymentState = PaymentUiState.Result(PaymentOutcome.TechnicalError),
                    selectedSimulation = PaymentSimulation.TECHNICAL_ERROR,
                    onSimulationSelected = {},
                    onConfirm = {},
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithText("Não foi possível concluir").assertExists()
        composeRule.onNodeWithText("Tente novamente ou volte ao catálogo.").assertExists()
    }
}
