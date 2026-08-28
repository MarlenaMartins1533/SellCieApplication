package com.marlena.martins.sellcieapplication.presentation.checkout

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.domain.model.PurchasedTicket
import com.marlena.martins.sellcieapplication.domain.usecase.PurchaseReceipt
import com.marlena.martins.sellcieapplication.presentation.theme.CieloTicketTheme
import com.marlena.martins.sellcieapplication.presentation.catalog.CatalogTestTags
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals

@RunWith(AndroidJUnit4::class)
class ReceiptScreenTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun approvedReceiptItemizesEveryEventAndQuantity() {
        var viewTicketsCount = 0
        composeRule.setContent {
            CieloTicketTheme {
                ReceiptScreen(
                    receipt = PurchaseReceipt(
                        purchaseId = "purchase-12345678",
                        items = listOf(
                            PurchasedTicket("music", "Music", 2, 2500),
                            PurchasedTicket("tech", "Tech", 1, 1800)
                        ),
                        totalInCents = 6800,
                        outcome = PaymentOutcome.Approved,
                        createdAt = 1_700_000_000_000
                    ),
                    outcome = PaymentOutcome.Approved,
                    onViewTickets = { viewTicketsCount += 1 },
                    onBack = {}
                )
            }
        }
        composeRule.onNodeWithText("Music").assertExists()
        composeRule.onNodeWithText("Tech").assertExists()
        composeRule.onNodeWithText("2 ingresso(s)", substring = true).assertExists()
        composeRule.onNodeWithText("1 ingresso(s)", substring = true).assertExists()
        composeRule.onNodeWithText("Quantidade total: 3").assertExists()
        composeRule.onNodeWithText("68,00", substring = true).assertExists()
        composeRule.onNodeWithText("Pagamento aprovado").assertExists()
        composeRule.onNodeWithText("Referência: 12345678").assertExists()
        composeRule.onNodeWithTag(CatalogTestTags.RECEIPT_VIEW_TICKETS_BUTTON).performClick()
        composeRule.runOnIdle { assertEquals(1, viewTicketsCount) }
    }

    @Test
    fun declinedReceiptShowsSafeGuidance() {
        composeRule.setContent {
            CieloTicketTheme {
                ReceiptScreen(
                    receipt = PurchaseReceipt(
                        purchaseId = "purchase-declined",
                        items = listOf(PurchasedTicket("music", "Music", 1, 2500)),
                        totalInCents = 2500,
                        outcome = PaymentOutcome.Declined,
                        createdAt = 1
                    ),
                    outcome = PaymentOutcome.Declined,
                    onViewTickets = {},
                    onBack = {}
                )
            }
        }
        composeRule.onNodeWithText("Nenhuma cobrança adicional foi enviada. Tente novamente.").assertExists()
        composeRule.onNodeWithTag(CatalogTestTags.RECEIPT_VIEW_TICKETS_BUTTON).assertDoesNotExist()
    }
}
