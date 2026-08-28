package com.marlena.martins.sellcieapplication.presentation.checkout

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.domain.model.PurchasedTicket
import com.marlena.martins.sellcieapplication.domain.usecase.PurchaseReceipt
import com.marlena.martins.sellcieapplication.presentation.catalog.CatalogTestTags
import com.marlena.martins.sellcieapplication.presentation.theme.CieloTicketTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PurchasedTicketsScreenTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun showsOneTicketAndQrFieldForEachPurchasedUnit() {
        composeRule.setContent {
            CieloTicketTheme {
                PurchasedTicketsScreen(
                    receipt = PurchaseReceipt(
                        purchaseId = "purchase-123",
                        items = listOf(
                            PurchasedTicket("music", "Music", 2, 2500),
                            PurchasedTicket("tech", "Tech", 1, 1800)
                        ),
                        totalInCents = 6800,
                        outcome = PaymentOutcome.Approved,
                        createdAt = 1
                    ),
                    onBack = {}
                )
            }
        }

        composeRule.onAllNodesWithText("Music").assertCountEquals(2)
        composeRule.onAllNodesWithText("Tech").assertCountEquals(1)
        composeRule.onAllNodesWithText("QR Code do ingresso", substring = true).assertCountEquals(3)
        composeRule.onNodeWithTag(CatalogTestTags.MY_TICKETS_BACK_BUTTON).assertExists()
    }
}
