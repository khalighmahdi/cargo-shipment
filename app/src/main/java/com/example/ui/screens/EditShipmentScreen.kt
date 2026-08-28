package com.example.ui.screens

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.ui.viewmodel.ShipmentViewModel
import com.example.util.ImageUtils
import com.example.util.JalaliCalendar
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditShipmentScreen(
    viewModel: ShipmentViewModel,
    shipmentId: Int,
    onBack: () -> Unit,
    onShipmentUpdated: () -> Unit
) {
    val context = LocalContext.current
    val shipmentState by viewModel.getShipmentById(shipmentId).collectAsState(initial = null)
    val scrollState = rememberScrollState()

    // Form states
    var cargoDescription by remember { mutableStateOf("") }
    var senderName by remember { mutableStateOf("") }
    var receiverName by remember { mutableStateOf("") }
    var sentBy by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedYear by remember { mutableStateOf(1405) }
    var selectedMonth by remember { mutableStateOf(1) }
    var selectedDay by remember { mutableStateOf(1) }
    var cargoImagePath by remember { mutableStateOf<String?>(null) }

    // Initialize form when shipment is loaded
    LaunchedEffect(shipmentState) {
        shipmentState?.let { shipment ->
            cargoDescription = shipment.cargoDescription
            senderName = shipment.senderName
            receiverName = shipment.receiverName
            sentBy = shipment.sentBy
            notes = shipment.notes
            selectedYear = shipment.jalaliYear
            selectedMonth = shipment.jalaliMonth
            selectedDay = shipment.jalaliDay
            cargoImagePath = shipment.imagePath
        }
    }

    // (Launchers same as AddShipmentScreen...)
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) { /* handle save */ }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { cargoImagePath = ImageUtils.saveImageToInternalStorage(context, it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Shipment", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize().verticalScroll(scrollState).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Text fields
            OutlinedTextField(value = cargoDescription, onValueChange = { cargoDescription = it }, label = { Text("Cargo Description") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = senderName, onValueChange = { senderName = it }, label = { Text("Sender Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = receiverName, onValueChange = { receiverName = it }, label = { Text("Receiver Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = sentBy, onValueChange = { sentBy = it }, label = { Text("Sent By") }, modifier = Modifier.fillMaxWidth())
            
            // Note: Simplified Date selectors for brevity in the edit screen (can reuse AddShipment logic)
            Text("Date: $selectedYear/$selectedMonth/$selectedDay", fontWeight = FontWeight.Bold)

            // Save Button
            Button(
                onClick = {
                    shipmentState?.let { original ->
                        val updated = original.copy(
                            cargoDescription = cargoDescription,
                            senderName = senderName,
                            receiverName = receiverName,
                            sentBy = sentBy,
                            notes = notes,
                            jalaliYear = selectedYear,
                            jalaliMonth = selectedMonth,
                            jalaliDay = selectedDay,
                            imagePath = cargoImagePath
                        )
                        viewModel.updateShipment(updated)
                        Toast.makeText(context, "Updated successfully", Toast.LENGTH_SHORT).show()
                        onShipmentUpdated()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Update Shipment")
            }
        }
    }
}
