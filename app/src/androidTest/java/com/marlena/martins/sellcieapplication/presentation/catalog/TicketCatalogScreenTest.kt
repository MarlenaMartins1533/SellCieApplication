package com.marlena.martins.sellcieapplication.presentation.catalog

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.marlena.martins.sellcieapplication.domain.model.Event
import com.marlena.martins.sellcieapplication.presentation.theme.CieloTicketTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TicketCatalogScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun quantitySelectorUpdatesSummaryAndRespectsAvailability() {
        var uiState by mutableStateOf(
            TicketUiState(
                events = listOf(Event("music", "Music", "18 set", "São Paulo", 2500, 2))
            )
        )

        composeRule.setContent {
            CieloTicketTheme {
                TicketCatalogScreen(
                    uiState = uiState,
                    onQuantityChange = { eventId, delta ->
                        val quantity = (uiState.quantityFor(eventId) + delta).coerceIn(0, 2)
                        uiState = uiState.copy(
                            quantitiesByEventId = if (quantity == 0) emptyMap() else mapOf(eventId to quantity),
                            totalInCents = quantity * 2500L
                        )
                    },
                    onContinue = {}
                )
            }
        }

        composeRule.onNodeWithTag(CatalogTestTags.increment("music")).performClick()
        composeRule.onNodeWithTag(CatalogTestTags.increment("music")).performClick()

        composeRule.onNodeWithTag(CatalogTestTags.ORDER_SUMMARY)
            .assertTextContains("2 ingresso(s)", substring = true)
        composeRule.onNodeWithTag(CatalogTestTags.ORDER_SUMMARY)
            .assertTextContains("50,00", substring = true)
        composeRule.onNodeWithTag(CatalogTestTags.increment("music")).assertIsNotEnabled()
    }
}
