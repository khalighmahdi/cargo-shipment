package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.Shipment
import kotlinx.coroutines.flow.Flow

@Dao
interface ShipmentDao {
    @Query("SELECT * FROM Shipments ORDER BY createdAt DESC")
    fun getAllShipments(): Flow<List<Shipment>>

    @Query("SELECT * FROM Shipments WHERE id = :id LIMIT 1")
    fun getShipmentById(id: Int): Flow<Shipment?>

    @Query("SELECT * FROM Shipments WHERE jalaliYear = :year AND jalaliMonth = :month ORDER BY jalaliDay ASC, createdAt DESC")
    fun getShipmentsByMonth(year: Int, month: Int): Flow<List<Shipment>>

    @Query("SELECT * FROM Shipments WHERE jalaliYear = :year AND jalaliMonth = :month AND jalaliDay = :day ORDER BY createdAt DESC")
    fun getShipmentsByDay(year: Int, month: Int, day: Int): Flow<List<Shipment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShipment(shipment: Shipment): Long

    @Delete
    suspend fun deleteShipment(shipment: Shipment)

    // Search queries - filter by sender name, receiver name, or cargo description
    @Query("SELECT * FROM Shipments WHERE senderName LIKE '%' || :query || '%' OR receiverName LIKE '%' || :query || '%' OR cargoDescription LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchShipments(query: String): Flow<List<Shipment>>

    @Query("SELECT * FROM Shipments WHERE status = :status ORDER BY createdAt DESC")
    fun getShipmentsByStatus(status: String): Flow<List<Shipment>>

    @Query("SELECT * FROM Shipments WHERE jalaliYear = :year AND jalaliMonth = :month AND (senderName LIKE '%' || :query || '%' OR receiverName LIKE '%' || :query || '%' OR cargoDescription LIKE '%' || :query || '%') ORDER BY jalaliDay ASC, createdAt DESC")
    fun searchShipmentsByMonth(year: Int, month: Int, query: String): Flow<List<Shipment>>

    @Query("UPDATE Shipments SET status = :status WHERE id = :id")
    suspend fun updateShipmentStatus(id: Int, status: String)

    @Query("SELECT * FROM Shipments ORDER BY createdAt DESC")
    suspend fun getAllShipmentsList(): List<Shipment>
}
