package com.marlena.martins.sellcieapplication.presentation.catalog

import java.text.NumberFormat
import java.util.Locale

fun formatCurrency(valueInCents: Long): String = NumberFormat
    .getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
    .format(valueInCents / 100.0)
