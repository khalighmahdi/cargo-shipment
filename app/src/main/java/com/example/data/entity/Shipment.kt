package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Shipments")
data class Shipment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cargoDescription: String,
    val senderName: String,
    val receiverName: String,
    val destination: String,
    val sentBy: String,
    val jalaliYear: Int,
    val jalaliMonth: Int,
    val jalaliDay: Int,
    val notes: String,
    val imagePath: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "در حال ارسال" // "در حال ارسال", "تحویل شده", "برگشتی"
) {
    companion object {
        const val STATUS_IN_TRANSIT = "در حال ارسال"
        const val STATUS_DELIVERED = "تحویل شده"
        const val STATUS_RETURNED = "برگشتی"

        val statusOptions = listOf(STATUS_IN_TRANSIT, STATUS_DELIVERED, STATUS_RETURNED)
    }
}
