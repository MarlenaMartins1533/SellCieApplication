package com.marlena.martins.sellcieapplication.presentation.catalog

object CatalogTestTags {
    const val RECEIPT_CARD = "receipt_card"
    const val RECEIPT_BACK_BUTTON = "receipt_back_button"
    const val ORDER_SUMMARY = "order_summary"
    const val CONTINUE_BUTTON = "continue_button"
    const val CHECKOUT_BACK_BUTTON = "checkout_back_button"

    fun increment(eventId: String) = "increment_$eventId"
    fun decrement(eventId: String) = "decrement_$eventId"
    fun quantity(eventId: String) = "quantity_$eventId"
}
