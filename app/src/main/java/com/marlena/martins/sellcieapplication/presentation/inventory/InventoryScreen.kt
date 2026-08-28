package com.marlena.martins.sellcieapplication.presentation.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.marlena.martins.sellcieapplication.domain.model.Event
import com.marlena.martins.sellcieapplication.presentation.catalog.CatalogTestTags
import com.marlena.martins.sellcieapplication.presentation.catalog.components.CieloAppHeader
import com.marlena.martins.sellcieapplication.presentation.catalog.formatCurrency
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun InventoryScreen(
    events: List<Event>,
    onAdjustInventory: (String, Int) -> Unit,
    onSetInventoryQuantity: (String, Int) -> Unit,
    onCreateEvent: (String, String, String, Long, Int) -> Boolean,
    onSaveInventory: () -> Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }
    var showSaveConfirmation by remember { mutableStateOf(false) }

    if (showSaveConfirmation) {
        SavedInventoryDialog(
            onBackToCatalog = {
                showSaveConfirmation = false
                onBack()
            }
        )
    }

    Scaffold(topBar = { CieloAppHeader("SellCie", "Controle de estoque") }) { padding ->
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("Controle de estoque", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Cadastre eventos e ajuste os ingressos disponíveis para venda.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Cadastrar evento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Nome do evento") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("Data e horário") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Local") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Preço (R$)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            label = { Text("Ingressos disponíveis") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        formError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        Button(
                            onClick = {
                                val priceInCents = price.toCents()
                                val availableTickets = quantity.toIntOrNull()
                                formError = when {
                                    title.isBlank() || date.isBlank() || location.isBlank() ->
                                        "Preencha nome, data e local do evento."
                                    priceInCents == null || priceInCents < 0 ->
                                        "Informe um preço válido."
                                    availableTickets == null || availableTickets < 0 ->
                                        "Informe uma quantidade válida."
                                    !onCreateEvent(title, date, location, priceInCents, availableTickets) ->
                                        "Não foi possível cadastrar o evento."
                                    else -> null
                                }
                                if (formError == null) {
                                    title = ""; date = ""; location = ""; price = ""; quantity = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth().semantics {
                                testTag = CatalogTestTags.INVENTORY_ADD_EVENT_BUTTON
                            }
                        ) { Text("Cadastrar evento") }
                    }
                }
            }
            item { Text("Eventos cadastrados", style = MaterialTheme.typography.titleLarge) }
            items(events, key = { it.id }) { event ->
                InventoryEventCard(event, onAdjustInventory, onSetInventoryQuantity)
            }
            item {
                Button(
                    onClick = { showSaveConfirmation = onSaveInventory() },
                    modifier = Modifier.fillMaxWidth().semantics {
                        testTag = CatalogTestTags.INVENTORY_SAVE_BUTTON
                    }
                ) { Text("Salvar alterações") }
            }
            item {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().semantics {
                        testTag = CatalogTestTags.INVENTORY_BACK_BUTTON
                    }
                ) { Text("Voltar ao catálogo") }
            }
        }
    }
}

@Composable
internal fun SavedInventoryDialog(onBackToCatalog: () -> Unit) {
    AlertDialog(
        onDismissRequest = onBackToCatalog,
        title = { Text("Alterações salvas") },
        text = { Text("As informações de estoque foram salvas com sucesso.") },
        confirmButton = {
            Button(onClick = onBackToCatalog) { Text("Voltar ao catálogo") }
        }
    )
}

@Composable
private fun InventoryEventCard(
    event: Event,
    onAdjustInventory: (String, Int) -> Unit,
    onSetInventoryQuantity: (String, Int) -> Unit
) {
    var quantityText by remember(event.id) { mutableStateOf(event.availableTickets.toString()) }
    LaunchedEffect(event.availableTickets) { quantityText = event.availableTickets.toString() }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(event.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("${event.date} • ${event.location}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatCurrency(event.priceInCents), color = MaterialTheme.colorScheme.primary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { onAdjustInventory(event.id, -1) },
                    enabled = event.availableTickets > 0,
                    modifier = Modifier.semantics { testTag = CatalogTestTags.inventoryDecrease(event.id) }
                ) { Text("−") }
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { value ->
                        if (value.all(Char::isDigit)) {
                            quantityText = value
                            value.toIntOrNull()?.let { onSetInventoryQuantity(event.id, it) }
                        }
                    },
                    label = { Text("Disponíveis") },
                    singleLine = true,
                    modifier = Modifier
                        .width(120.dp)
                        .semantics { testTag = CatalogTestTags.inventoryQuantity(event.id) }
                )
                Button(
                    onClick = { onAdjustInventory(event.id, 1) },
                    modifier = Modifier.semantics { testTag = CatalogTestTags.inventoryIncrease(event.id) }
                ) { Text("+") }
            }
        }
    }
}

private fun String.toCents(): Long? = runCatching {
    BigDecimal(trim().replace(',', '.')).movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact()
}.getOrNull()
