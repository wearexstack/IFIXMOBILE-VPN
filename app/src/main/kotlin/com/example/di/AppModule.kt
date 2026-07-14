package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.api.VpnApiService
import com.example.data.db.AppDatabase
import com.example.data.repository.VpnRepository
import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object AppModule {
    private const val BASE_URL = "https://api.vpn.example.com/"

    fun provideDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "vpn_database"
        ).build()
    }

    fun provideApiService(): VpnApiService {
        val moshi = Moshi.Builder().build()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(VpnApiService::class.java)
    }

    fun provideRepository(context: Context): VpnRepository {
        return VpnRepository(
            apiService = provideApiService(),
            database = provideDatabase(context)
        )
    }
}