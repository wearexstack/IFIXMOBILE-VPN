package com.example.data.repository

import com.example.data.api.VpnApiService
import com.example.data.db.AppDatabase
import com.example.data.models.VpnServer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class VpnRepository(
    private val apiService: VpnApiService,
    private val database: AppDatabase
) {
    fun getAllServers(): Flow<List<VpnServer>> = database.vpnServerDao().getAllServers()

    fun getServersByCountry(country: String): Flow<List<VpnServer>> =
        database.vpnServerDao().getServersByCountry(country)

    suspend fun refreshServers() {
        try {
            val servers = apiService.getVpnServers()
            database.vpnServerDao().deleteAllServers()
            database.vpnServerDao().insertServers(servers)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getRecommendedServer(): VpnServer? {
        return try {
            apiService.getRecommendedServer()
        } catch (e: Exception) {
            // Fallback to local database
            getAllServers().firstOrNull()?.firstOrNull()
        }
    }
}