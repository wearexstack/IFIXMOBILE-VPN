package com.example.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.models.VpnServer
import com.example.data.db.dao.VpnServerDao

@Database(entities = [VpnServer::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vpnServerDao(): VpnServerDao
}