package com.example.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.models.VpnServer
import kotlinx.coroutines.flow.Flow

@Dao
interface VpnServerDao {
    @Query("SELECT * FROM vpn_servers ORDER BY latency ASC")
    fun getAllServers(): Flow<List<VpnServer>>

    @Query("SELECT * FROM vpn_servers WHERE country = :country")
    fun getServersByCountry(country: String): Flow<List<VpnServer>>

    @Query("SELECT * FROM vpn_servers WHERE id = :serverId")
    suspend fun getServerById(serverId: String): VpnServer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServers(servers: List<VpnServer>)

    @Query("DELETE FROM vpn_servers")
    suspend fun deleteAllServers()
}