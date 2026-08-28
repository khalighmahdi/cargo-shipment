package com.example.di

import android.content.Context
import com.example.data.dao.ShipmentDao
import com.example.data.db.AppDatabase
import com.example.data.repository.ShipmentRepository
import com.example.util.LocalServerManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideShipmentDao(database: AppDatabase): ShipmentDao {
        return database.shipmentDao()
    }

    @Provides
    @Singleton
    fun provideRepository(shipmentDao: ShipmentDao): ShipmentRepository {
        return ShipmentRepository(shipmentDao)
    }

    @Provides
    @Singleton
    fun provideLocalServerManager(
        @ApplicationContext context: Context,
        repository: ShipmentRepository
    ): LocalServerManager {
        return LocalServerManager(context, repository)
    }
}
