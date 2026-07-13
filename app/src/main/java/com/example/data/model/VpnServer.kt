package com.example.data.model

data class VpnServer(
    val id: String,
    val countryName: String,
    val countryFlag: String, // Emoji flag e.g., "🇩🇪"
    val city: String,
    val ipAddress: String,
    val pingMs: Int,
    val loadPercentage: Int, // 0-100
    val isPremium: Boolean,
    val isActive: Boolean = true,
    val isFavorite: Boolean = false
) {
    val statusText: String
        get() = if (loadPercentage < 40) "خلوت" else if (loadPercentage < 75) "متوسط" else "شلوغ"
}
