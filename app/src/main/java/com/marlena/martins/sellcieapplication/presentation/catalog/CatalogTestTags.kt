package com.marlena.martins.sellcieapplication.presentation.catalog

object CatalogTestTags {
    const val ORDER_SUMMARY = "order_summary"
    const val CONTINUE_BUTTON = "continue_button"

    fun increment(eventId: String) = "increment_$eventId"
    fun decrement(eventId: String) = "decrement_$eventId"
    fun quantity(eventId: String) = "quantity_$eventId"
}
