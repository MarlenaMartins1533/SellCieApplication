package com.marlena.martins.sellcieapplication.domain.model

data class CartItem(
    val event: Event,
    val quantity: Int
) {
    init {
        require(quantity > 0) { "A quantidade deve ser maior que zero." }
    }
}
