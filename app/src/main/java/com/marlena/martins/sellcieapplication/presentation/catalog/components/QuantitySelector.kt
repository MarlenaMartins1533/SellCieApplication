package com.marlena.martins.sellcieapplication.presentation.catalog.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import com.marlena.martins.sellcieapplication.presentation.catalog.CatalogTestTags

@Composable
fun QuantitySelector(
    eventId: String,
    quantity: Int,
    maxQuantity: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TextButton(
            onClick = onDecrease,
            enabled = quantity > 0,
            modifier = Modifier.semantics { testTag = CatalogTestTags.decrement(eventId) }
        ) {
            Text("−")
        }
        Text(
            text = quantity.toString(),
            modifier = Modifier
                .width(28.dp)
                .semantics { testTag = CatalogTestTags.quantity(eventId) }
        )
        FilledTonalButton(
            onClick = onIncrease,
            enabled = quantity < maxQuantity,
            modifier = Modifier.semantics { testTag = CatalogTestTags.increment(eventId) }
        ) {
            Text("+")
        }
    }
}
