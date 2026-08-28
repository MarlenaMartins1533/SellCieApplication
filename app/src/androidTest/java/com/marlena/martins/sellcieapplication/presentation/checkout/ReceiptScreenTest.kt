package com.marlena.martins.sellcieapplication.presentation.checkout

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.marlena.martins.sellcieapplication.domain.model.Event
import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.domain.usecase.PurchaseReceipt
import com.marlena.martins.sellcieapplication.presentation.theme.CieloTicketTheme
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReceiptScreenTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun approvedReceiptShowsAllPurchaseFields() {
        composeRule.setContent {
            CieloTicketTheme {
                ReceiptScreen(
                    receipt = PurchaseReceipt(
                        "purchase-12345678", Event("music", "Music", "18 set", "São Paulo", 2500, 10),
                        2, 5000, PaymentOutcome.Approved, 1_700_000_000_000
                    ),
                    outcome = PaymentOutcome.Approved,
                    onBack = {}
                )
            }
        }
        composeRule.onNodeWithText("Evento: Music").assertExists()
        composeRule.onNodeWithText("Quantidade: 2").assertExists()
        composeRule.onNodeWithText("50,00", substring = true).assertExists()
        composeRule.onNodeWithText("Pagamento aprovado").assertExists()
        composeRule.onNodeWithText("Referência: 12345678").assertExists()
    }

    @Test
    fun declinedReceiptShowsSafeGuidance() {
        composeRule.setContent {
            CieloTicketTheme {
                ReceiptScreen(
                    receipt = PurchaseReceipt(
                        "purchase-declined", Event("music", "Music", "18 set", "São Paulo", 2500, 10),
                        1, 2500, PaymentOutcome.Declined, 1
                    ),
                    outcome = PaymentOutcome.Declined,
                    onBack = {}
                )
            }
        }
        composeRule.onNodeWithText("Nenhuma cobrança adicional foi enviada. Tente novamente.").assertExists()
    }
}
