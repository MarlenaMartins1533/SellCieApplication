package com.marlena.martins.sellcieapplication.presentation.checkout

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
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
    fun processingDoesNotExposeLocalTestControls() {
        composeRule.setContent {
            CieloTicketTheme {
                CheckoutScreen(
                    selectedTicketCount = 2,
                    totalInCents = 5000,
                    paymentState = PaymentUiState.Processing("purchase-1"),
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithText("Cielo Ingressos").assertExists()
        composeRule.onNodeWithText("Processando pagamento…").assertExists()
        composeRule.onAllNodesWithText("Confirmar pagamento").assertCountEquals(0)
        composeRule.onAllNodesWithText("Resultado local para teste").assertCountEquals(0)
    }

    @Test
    fun technicalErrorShowsSandboxConfigurationGuidance() {
        composeRule.setContent {
            CieloTicketTheme {
                CheckoutScreen(
                    selectedTicketCount = 1,
                    totalInCents = 2500,
                    paymentState = PaymentUiState.Result(PaymentOutcome.TechnicalError),
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithText("Não foi possível concluir").assertExists()
        composeRule.onNodeWithText("Tente novamente ou volte ao catálogo.").assertExists()
    }
}
