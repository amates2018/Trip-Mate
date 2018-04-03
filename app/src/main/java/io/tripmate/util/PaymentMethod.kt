package io.tripmate.util

/**
 * Class of Payment methods
 */
enum class PaymentMethod(val value: String) {

    MTN_MOMO("MTN"),
    VODAFONE_MOMO("Vodafone"),
    AIRTEL_CASH("Airtel"),
    CREDIT_CARD("Credit Card"),
    TIGO_CASH("Tigo");
}