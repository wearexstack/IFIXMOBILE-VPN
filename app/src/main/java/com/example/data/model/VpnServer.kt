package com.example.data.model

data class VpnServer(
    val id: String,
    val countryName: String,
    val countryFlag: String,
    val city: String,
    val ipAddress: String,
    val pingMs: Int,
    val loadPercentage: Int,
    val isPremium: Boolean,
    val isActive: Boolean = true,
    val isFavorite: Boolean = false,
    /** Share-link for real tunnel: vless:// trojan:// vmess:// ss:// */
    val configUri: String = ""
) {
    val statusText: String
        get() = if (loadPercentage < 40) "خلوت" else if (loadPercentage < 75) "متوسط" else "شلوغ"

    val canConnectReal: Boolean
        get() = configUri.isNotBlank()
}
