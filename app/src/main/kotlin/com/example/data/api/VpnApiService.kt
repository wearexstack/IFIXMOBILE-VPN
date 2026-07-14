package com.example.data.api

import com.example.data.models.VpnServer
import retrofit2.http.GET
import retrofit2.http.Headers

interface VpnApiService {
    @Headers("Content-Type: application/json")
    @GET("api/servers")
    suspend fun getVpnServers(): List<VpnServer>

    @GET("api/servers/recommended")
    suspend fun getRecommendedServer(): VpnServer
}