package com.example

import android.app.Application
import com.example.data.db.AppDatabase
import com.example.data.repository.ShipmentRepository
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CargoApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { ShipmentRepository(database.shipmentDao()) }
}
