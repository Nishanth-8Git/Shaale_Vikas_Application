package com.example.shaalevikas.ui

import java.text.NumberFormat
import java.util.*

/**
 * Shared utility to format currency consistently throughout the app.
 */
fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
    return format.format(amount)
}
