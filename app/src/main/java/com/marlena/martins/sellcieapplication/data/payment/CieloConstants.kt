package com.marlena.martins.sellcieapplication.data.payment

object CieloConstants {
    // Deep Link URIs
    const val SCHEME = "lio"
    const val HOST_PAYMENT = "payment"
    const val PAYMENT_URI = "$SCHEME://$HOST_PAYMENT"

    // Callback Contract
    const val CALLBACK_SCHEME = "sellcie"
    const val CALLBACK_HOST = "payment-result"
    const val CALLBACK_URI = "$CALLBACK_SCHEME://$CALLBACK_HOST"
    
    // Callback Parameters
    const val PARAM_RESPONSE = "response"
    const val PARAM_RESPONSE_CODE = "responsecode"

    // Magic Values (Sandbox Codes)
    const val CODE_SUCCESS = 0
    const val CODE_INSUFFICIENT_FUNDS = 51
    const val CODE_EXPIRED_CARD = 54
    const val CODE_NOT_AUTHORIZED = 5
    const val CODE_DECLINED = 25
    const val CODE_CANCELED = 78
    const val CODE_USER_CANCELED = 1
    const val CODE_TIMEOUT = 98
    const val CODE_SYSTEM_ERROR = 99

    // Metadata Keys
    const val KEY_AUTH_CODE = "authCode"
    const val KEY_CIELO_CODE = "cieloCode"
    const val KEY_BRAND = "brand"
    const val KEY_CARD_MASK = "mask"
    const val KEY_TERMINAL = "terminal"
    const val KEY_REASON = "reason"
    
    // Defaults
    const val DEFAULT_EMAIL = "vendedor@sellcie.com.br"
    const val DEFAULT_UNIT_OF_MEASURE = "unidade"
    const val DEFAULT_INSTALLMENTS = 1
    const val DEFAULT_PAYMENT_CODE = "DEBITO_AVISTA"
}
