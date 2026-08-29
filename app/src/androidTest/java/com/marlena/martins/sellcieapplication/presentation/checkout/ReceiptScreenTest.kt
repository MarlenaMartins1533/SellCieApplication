package com.marlena.martins.sellcieapplication.presentation.checkout

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.domain.model.PurchasedTicket
import com.marlena.martins.sellcieapplication.domain.usecase.PurchaseReceipt
import com.marlena.martins.sellcieapplication.presentation.theme.CieloTicketTheme
import com.marlena.martins.sellcieapplication.presentation.catalog.CatalogTestTags
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.marlena.martins.sellcieapplication.data.payment.CieloConstants
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals

@RunWith(AndroidJUnit4::class)
class ReceiptScreenTest {
    @get:Rule 
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun approvedReceiptDisplaysItemDetailsAndCieloMetadata() {
        var viewTicketsCount = 0
        composeRule.setContent {
            CieloTicketTheme {
                ReceiptScreen(
                    receipt = PurchaseReceipt(
                        purchaseId = "purchase-12345678",
                        items = listOf(
                            PurchasedTicket("music", "Music", 2, 2500),
                        ),
                        totalInCents = 5000,
                        outcome = PaymentOutcome.Approved,
                        createdAt = 1_700_000_000_000,
                        cieloMetadata = mapOf(
                            CieloConstants.KEY_BRAND to "Visa",
                            CieloConstants.KEY_CIELO_CODE to "998877"
                        )
                    ),
                    outcome = PaymentOutcome.Approved,
                    onViewTickets = { viewTicketsCount += 1 },
                    onBack = {}
                )
            }
        }
        composeRule.onNodeWithText("Music").assertExists()
        composeRule.onNodeWithText("2 ingresso(s)", substring = true).assertExists()
        composeRule.onNodeWithText("50,00", substring = true).assertExists()
        composeRule.onNodeWithText("Pagamento aprovado").assertExists()
        
        // Cielo Metadata
        composeRule.onNodeWithText("Bandeira: Visa").assertExists()
        composeRule.onNodeWithText("NSU: 998877").assertExists()

        composeRule.onNodeWithTag(CatalogTestTags.RECEIPT_VIEW_TICKETS_BUTTON).performClick()
        composeRule.runOnIdle { assertEquals(1, viewTicketsCount) }
    }

    @Test
    fun declinedReceiptShowsReasonWhenAvailable() {
        composeRule.setContent {
            CieloTicketTheme {
                ReceiptScreen(
                    receipt = PurchaseReceipt(
                        purchaseId = "purchase-declined",
                        items = listOf(PurchasedTicket("music", "Music", 1, 2500)),
                        totalInCents = 2500,
                        outcome = PaymentOutcome.Declined,
                        createdAt = 1,
                        cieloMetadata = mapOf(CieloConstants.KEY_REASON to "Saldo insuficiente")
                    ),
                    outcome = PaymentOutcome.Declined,
                    onViewTickets = {},
                    onBack = {}
                )
            }
        }
        composeRule.onNodeWithText("Pagamento não aprovado").assertExists()
        composeRule.onNodeWithText("Motivo: Saldo insuficiente").assertExists()
        composeRule.onNodeWithTag(CatalogTestTags.RECEIPT_VIEW_TICKETS_BUTTON).assertDoesNotExist()
    }
}
