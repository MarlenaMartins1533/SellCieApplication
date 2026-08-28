package com.marlena.martins.sellcieapplication.presentation.checkout

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.marlena.martins.sellcieapplication.presentation.catalog.CatalogTestTags
import com.marlena.martins.sellcieapplication.presentation.catalog.PaymentUiState
import com.marlena.martins.sellcieapplication.presentation.theme.CieloTicketTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals

@RunWith(AndroidJUnit4::class)
class CheckoutScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun confirmationRequiresAnExplicitUserAction() {
        var confirmationCount = 0
        composeRule.setContent {
            CieloTicketTheme {
                CheckoutScreen(
                    selectedTicketCount = 2,
                    totalInCents = 5000,
                    paymentState = PaymentUiState.Idle,
                    onConfirm = { confirmationCount += 1 },
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithText("Confirme sua compra").assertExists()
        composeRule.onNodeWithTag(CatalogTestTags.CHECKOUT_CONFIRM_BUTTON).performClick()
        composeRule.onNodeWithTag(CatalogTestTags.CHECKOUT_BACK_BUTTON).assertExists()
        composeRule.runOnIdle { assertEquals(1, confirmationCount) }
    }

    @Test
    fun processingHidesTheConfirmationActions() {
        composeRule.setContent {
            CieloTicketTheme {
                CheckoutScreen(
                    selectedTicketCount = 1,
                    totalInCents = 2500,
                    paymentState = PaymentUiState.Processing("purchase-1"),
                    onConfirm = {},
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithText("Processando pagamento…").assertExists()
        composeRule.onNodeWithTag(CatalogTestTags.CHECKOUT_CONFIRM_BUTTON).assertDoesNotExist()
    }
}
