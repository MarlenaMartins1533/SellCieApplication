package com.marlena.martins.sellcieapplication.data.payment.model

data class OrderRequest(
    val clientID: String,
    val accessToken: String,
    val value: Long,
    val paymentCode: String?,
    val installments: Int,
    val email: String,
    val merchantCode: String?,
    val reference: String,
    val items: MutableList<Item>
)
