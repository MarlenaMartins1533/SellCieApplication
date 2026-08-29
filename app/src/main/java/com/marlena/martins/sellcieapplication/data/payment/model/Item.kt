package com.marlena.martins.sellcieapplication.data.payment.model

data class Item(
    val sku: String,
    val name: String,
    val unitPrice: Long,
    val quantity: Int,
    val unitOfMeasure: String
)
