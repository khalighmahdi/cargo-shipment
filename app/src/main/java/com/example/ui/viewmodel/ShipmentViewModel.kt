package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.entity.Shipment
import com.example.data.repository.ShipmentRepository
import com.example.util.ExportUtil
import com.example.util.LocalServerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShipmentViewModel @Inject constructor(
    application: android.app.Application,
    private val repository: ShipmentRepository,
    private val serverManager: LocalServerManager
) : androidx.lifecycle.AndroidViewModel(application) {

    private val _serverIp = MutableStateFlow<String?>(null)
    val serverIp: StateFlow<String?> = _serverIp.asStateFlow()

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtered shipments based on search query
    val filteredShipments: StateFlow<List<Shipment>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.allShipments
            } else {
                repository.searchShipments(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        startSharing()
    }

    fun startSharing() {
        viewModelScope.launch {
            serverManager.startServer()
            _serverIp.value = serverManager.getLocalIpAddress()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    val allShipments: StateFlow<List<Shipment>> = repository.allShipments
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getShipmentById(id: Int): Flow<Shipment?> {
        return repository.getShipmentById(id)
    }

    fun getShipmentsByMonth(year: Int, month: Int): Flow<List<Shipment>> {
        return repository.getShipmentsByMonth(year, month)
    }

    fun searchShipmentsByMonth(year: Int, month: Int, query: String): Flow<List<Shipment>> {
        return repository.searchShipmentsByMonth(year, month, query)
    }

    fun getShipmentsByDay(year: Int, month: Int, day: Int): Flow<List<Shipment>> {
        return repository.getShipmentsByDay(year, month, day)
    }

    fun insertShipment(
        cargoDescription: String,
        senderName: String,
        receiverName: String,
        sentBy: String,
        jalaliYear: Int,
        jalaliMonth: Int,
        jalaliDay: Int,
        notes: String,
        imagePath: String?,
        status: String = Shipment.STATUS_IN_TRANSIT,
        onComplete: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val shipment = Shipment(
                cargoDescription = cargoDescription.trim(),
                senderName = senderName.trim(),
                receiverName = receiverName.trim(),
                sentBy = sentBy.trim(),
                jalaliYear = jalaliYear,
                jalaliMonth = jalaliMonth,
                jalaliDay = jalaliDay,
                notes = notes.trim(),
                imagePath = imagePath,
                status = status
            )
            val newId = repository.insertShipment(shipment)
            onComplete(newId)
        }
    }

    fun deleteShipment(shipment: Shipment) {
        viewModelScope.launch {
            repository.deleteShipment(shipment)
        }
    }

    fun updateShipment(shipment: Shipment) {
        viewModelScope.launch {
            repository.insertShipment(shipment) // Room's insert with OnConflictStrategy.REPLACE
        }
    }

    fun updateShipmentStatus(id: Int, status: String) {
        viewModelScope.launch {
            repository.updateShipmentStatus(id, status)
        }
    }

    // Export to Excel
    fun exportToExcel(context: Context, shipments: List<Shipment>, fileName: String = "shipments") {
        viewModelScope.launch {
            ExportUtil.exportToExcel(context, shipments, fileName)
        }
    }

    // Export to PDF
    fun exportToPdf(context: Context, shipments: List<Shipment>, fileName: String = "shipments") {
        viewModelScope.launch {
            ExportUtil.exportToPdf(context, shipments, fileName)
        }
    }
}
