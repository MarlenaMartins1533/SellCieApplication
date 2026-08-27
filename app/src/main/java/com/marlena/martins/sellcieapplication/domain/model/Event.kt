package com.marlena.martins.sellcieapplication.domain.model

data class Event(
    val id: String,
    val title: String,
    val date: String,
    val location: String,
    val priceInCents: Long,
    val availableTickets: Int
)
