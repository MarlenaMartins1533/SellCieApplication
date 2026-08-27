package com.marlena.martins.sellcieapplication.domain.usecase

import com.marlena.martins.sellcieapplication.domain.model.CartItem

class CalculateOrderTotal {
    operator fun invoke(items: List<CartItem>): Long =
        items.sumOf { it.event.priceInCents * it.quantity }
}
