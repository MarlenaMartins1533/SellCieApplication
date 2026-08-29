package com.marlena.martins.sellcieapplication.data.payment.model

enum class OrderType(val value: String) {
    PAYMENT("PAYMENT"),
    AUTHORIZATION("AUTHORIZATION");

    fun identifier(): String = value

    companion object {
        fun from(type: String?): OrderType =
            if (type != null && AUTHORIZATION.identifier() == type) AUTHORIZATION else PAYMENT
    }
}
