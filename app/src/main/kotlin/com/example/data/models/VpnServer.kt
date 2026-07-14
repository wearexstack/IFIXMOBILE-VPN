package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "vpn_servers")
data class VpnServer(
    @PrimaryKey
    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "country")
    val country: String,
    @Json(name = "countryCode")
    val countryCode: String,
    @Json(name = "address")
    val address: String,
    @Json(name = "port")
    val port: Int,
    @Json(name = "protocol")
    val protocol: String,
    @Json(name = "latency")
    val latency: Int? = null,
    @Json(name = "load")
    val load: Float? = null,
    @Json(name = "isPremium")
    val isPremium: Boolean = false
)