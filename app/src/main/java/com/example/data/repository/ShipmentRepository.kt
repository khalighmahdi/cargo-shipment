package com.example.data.repository

import com.example.data.dao.ShipmentDao
import com.example.data.entity.Shipment
import kotlinx.coroutines.flow.Flow

class ShipmentRepository(private val shipmentDao: ShipmentDao) {
    val allShipments: Flow<List<Shipment>> = shipmentDao.getAllShipments()

    fun getShipmentById(id: Int): Flow<Shipment?> = shipmentDao.getShipmentById(id)

    fun getShipmentsByMonth(year: Int, month: Int): Flow<List<Shipment>> = 
        shipmentDao.getShipmentsByMonth(year, month)

    fun getShipmentsByDay(year: Int, month: Int, day: Int): Flow<List<Shipment>> = 
        shipmentDao.getShipmentsByDay(year, month, day)

    suspend fun insertShipment(shipment: Shipment): Long = shipmentDao.insertShipment(shipment)

    suspend fun deleteShipment(shipment: Shipment) = shipmentDao.deleteShipment(shipment)

    // Search functionality
    fun searchShipments(query: String): Flow<List<Shipment>> = 
        shipmentDao.searchShipments(query)

    fun getShipmentsByStatus(status: String): Flow<List<Shipment>> = 
        shipmentDao.getShipmentsByStatus(status)

    fun searchShipmentsByMonth(year: Int, month: Int, query: String): Flow<List<Shipment>> = 
        shipmentDao.searchShipmentsByMonth(year, month, query)

    suspend fun updateShipmentStatus(id: Int, status: String) = 
        shipmentDao.updateShipmentStatus(id, status)

    // For export - get all shipments as a list
    suspend fun getAllShipmentsList(): List<Shipment> = shipmentDao.getAllShipmentsList()
}
