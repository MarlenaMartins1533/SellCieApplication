package com.marlena.martins.sellcieapplication.presentation.inventory

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.marlena.martins.sellcieapplication.domain.model.Event
import com.marlena.martins.sellcieapplication.presentation.catalog.CatalogTestTags
import com.marlena.martins.sellcieapplication.presentation.theme.CieloTicketTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InventoryScreenTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun exposesEventStockControlsAndCallsTheirActions() {
        var adjustedEventId = ""
        var delta = 0
        var editedQuantity = -1
        composeRule.setContent {
            CieloTicketTheme {
                InventoryScreen(
                    events = listOf(Event("music", "Music", "18 set", "São Paulo", 2500, 1)),
                    onAdjustInventory = { eventId, adjustment -> adjustedEventId = eventId; delta = adjustment },
                    onSetInventoryQuantity = { _, quantity -> editedQuantity = quantity },
                    onCreateEvent = { _, _, _, _, _ -> true },
                    onSaveInventory = { true },
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithTag(CatalogTestTags.inventoryDecrease("music")).performClick()
        composeRule.runOnIdle {
            assertEquals("music", adjustedEventId)
            assertEquals(-1, delta)
        }
        composeRule.onNodeWithTag(CatalogTestTags.inventoryIncrease("music")).performClick()
        composeRule.runOnIdle { assertEquals(1, delta) }
        composeRule.onNodeWithTag(CatalogTestTags.inventoryQuantity("music")).performTextClearance()
        composeRule.onNodeWithTag(CatalogTestTags.inventoryQuantity("music")).performTextInput("4")
        composeRule.runOnIdle { assertEquals(4, editedQuantity) }
        composeRule.onNodeWithTag(CatalogTestTags.INVENTORY_ADD_EVENT_BUTTON).assertExists()
    }

    @Test
    fun disablesDecreasingWhenNoTicketsAreAvailable() {
        composeRule.setContent {
            CieloTicketTheme {
                InventoryScreen(
                    events = listOf(Event("sold-out", "Lotado", "18 set", "São Paulo", 2500, 0)),
                    onAdjustInventory = { _, _ -> },
                    onSetInventoryQuantity = { _, _ -> },
                    onCreateEvent = { _, _, _, _, _ -> true },
                    onSaveInventory = { true },
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithText("Disponíveis").assertExists()
        composeRule.onNodeWithTag(CatalogTestTags.inventoryDecrease("sold-out")).assertIsNotEnabled()
    }

    @Test
    fun savedChangesDialogReturnsToCatalogAfterConfirmation() {
        var returnCount = 0
        composeRule.setContent {
            CieloTicketTheme { SavedInventoryDialog { returnCount += 1 } }
        }

        composeRule.onNodeWithText("Alterações salvas").assertExists()
        composeRule.onNodeWithText("Voltar ao catálogo").performClick()
        composeRule.runOnIdle { assertEquals(1, returnCount) }
    }

}
