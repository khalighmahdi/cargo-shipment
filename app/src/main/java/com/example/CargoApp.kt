package com.example

import android.app.Application
import com.example.data.db.AppDatabase
import com.example.data.repository.ShipmentRepository
import com.example.util.LocalServerManager

class CargoApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { ShipmentRepository(database.shipmentDao()) }
    val serverManager by lazy { LocalServerManager(applicationContext, repository) }
}
