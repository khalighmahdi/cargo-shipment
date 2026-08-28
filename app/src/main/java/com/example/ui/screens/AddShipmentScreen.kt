package com.example.ui.screens

import android.Manifest
import android.content.Context
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
fun AddShipmentScreen(
    viewModel: ShipmentViewModel,
    prefilledYear: Int?,
    prefilledMonth: Int?,
    prefilledDay: Int?,
    onBack: () -> Unit,
    onShipmentSaved: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Determine default dates
    val today = remember { JalaliCalendar.currentJalaliDate() }
    val defaultYear = prefilledYear ?: today.year
    val defaultMonth = prefilledMonth ?: today.month
    val defaultDay = prefilledDay ?: today.day

    // Form states
    var cargoDescription by remember { mutableStateOf("") }
    var senderName by remember { mutableStateOf("") }
    var receiverName by remember { mutableStateOf("") }
    var sentBy by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Date states
    var selectedYear by remember { mutableStateOf(defaultYear) }
    var selectedMonth by remember { mutableStateOf(defaultMonth) }
    var selectedDay by remember { mutableStateOf(defaultDay) }

    // Cargo image state
    var cargoImagePath by remember { mutableStateOf<String?>(null) }

    // File and Uri state for camera capture
    var tempPhotoFile by remember { mutableStateOf<File?>(null) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Photo capture launchers
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempPhotoUri?.let { uri ->
                val localPath = ImageUtils.saveImageToInternalStorage(context, uri)
                if (localPath != null) {
                    cargoImagePath = localPath
                } else {
                    Toast.makeText(context, "Failed to save photo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val localPath = ImageUtils.saveImageToInternalStorage(context, uri)
            if (localPath != null) {
                cargoImagePath = localPath
            } else {
                Toast.makeText(context, "Failed to load selected image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Permission launcher for Camera
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, launch camera
            try {
                val directory = File(context.cacheDir, "camera_previews").apply { if (!exists()) mkdirs() }
                val file = File.createTempFile("cargo_cap_", ".jpg", directory)
                val authority = "${context.packageName}.fileprovider"
                val uri = FileProvider.getUriForFile(context, authority, file)
                tempPhotoFile = file
                tempPhotoUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error starting camera: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Camera permission is required to capture photos.", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Cargo Shipment", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Form Cards
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Shipment Basic Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Cargo Description
                    OutlinedTextField(
                        value = cargoDescription,
                        onValueChange = { cargoDescription = it },
                        label = { Text("Cargo Description") },
                        placeholder = { Text("e.g. 5 boxes of electronics, steel sheets") },
                        leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cargo_description_input"),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    // Sender Name
                    OutlinedTextField(
                        value = senderName,
                        onValueChange = { senderName = it },
                        label = { Text("Sender Name") },
                        placeholder = { Text("Who is shipping this cargo?") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sender_name_input"),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    // Receiver Name
                    OutlinedTextField(
                        value = receiverName,
                        onValueChange = { receiverName = it },
                        label = { Text("Receiver Name") },
                        placeholder = { Text("Who will receive this cargo?") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("receiver_name_input"),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    // Sent By / Transport Method
                    OutlinedTextField(
                        value = sentBy,
                        onValueChange = { sentBy = it },
                        label = { Text("Sent By") },
                        placeholder = { Text("e.g. Alborz Trucking, Driver Reza, Air Cargo") },
                        leadingIcon = { Icon(Icons.Default.LocalShipping, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sent_by_input"),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    // Status Selector
                    var statusExpanded by remember { mutableStateOf(false) }
                    var selectedStatus by remember { mutableStateOf(com.example.data.entity.Shipment.STATUS_IN_TRANSIT) }
                    Text(
                        "Shipment Status",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ExposedDropdownMenuBox(
                        expanded = statusExpanded,
                        onExpandedChange = { statusExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedStatus,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Status") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("status_dropdown"),
                            shape = RoundedCornerShape(16.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false }
                        ) {
                            com.example.data.entity.Shipment.statusOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        selectedStatus = option
                                        statusExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Jalali Date Picker Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Jalali Shipment Date",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "${selectedDay} ${JalaliCalendar.getJalaliMonthName(selectedMonth)} ${selectedYear}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Dropdown selectors for Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Year Dropdown
                        var yearExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedCard(
                                onClick = { yearExpanded = true },
                                modifier = Modifier.fillMaxWidth().testTag("year_dropdown_trigger")
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Year", style = MaterialTheme.typography.labelSmall)
                                    Text("$selectedYear", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                            DropdownMenu(
                                expanded = yearExpanded,
                                onDismissRequest = { yearExpanded = false }
                            ) {
                                ((today.year - 2)..(today.year + 5)).forEach { yr ->
                                    DropdownMenuItem(
                                        text = { Text("$yr") },
                                        onClick = {
                                            selectedYear = yr
                                            yearExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Month Dropdown
                        var monthExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1.5f)) {
                            OutlinedCard(
                                onClick = { monthExpanded = true },
                                modifier = Modifier.fillMaxWidth().testTag("month_dropdown_trigger")
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Month", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = JalaliCalendar.getJalaliMonthNameFa(selectedMonth),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = monthExpanded,
                                onDismissRequest = { monthExpanded = false }
                            ) {
                                (1..12).forEach { mth ->
                                    DropdownMenuItem(
                                        text = { Text(JalaliCalendar.getJalaliMonthName(mth)) },
                                        onClick = {
                                            selectedMonth = mth
                                            monthExpanded = false
                                            // Adjust day limit if day is higher than month max days
                                            val maxD = when (selectedMonth) {
                                                in 1..6 -> 31
                                                in 7..11 -> 30
                                                12 -> {
                                                    val isLeap = (selectedYear % 33) in intArrayOf(1, 5, 9, 13, 17, 22, 26, 30)
                                                    if (isLeap) 30 else 29
                                                }
                                                else -> 30
                                            }
                                            if (selectedDay > maxD) {
                                                selectedDay = maxD
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        // Day Dropdown
                        var dayExpanded by remember { mutableStateOf(false) }
                        val maxDays = remember(selectedYear, selectedMonth) {
                            when (selectedMonth) {
                                in 1..6 -> 31
                                in 7..11 -> 30
                                12 -> {
                                    val isLeap = (selectedYear % 33) in intArrayOf(1, 5, 9, 13, 17, 22, 26, 30)
                                    if (isLeap) 30 else 29
                                }
                                else -> 30
                            }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedCard(
                                onClick = { dayExpanded = true },
                                modifier = Modifier.fillMaxWidth().testTag("day_dropdown_trigger")
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Day", style = MaterialTheme.typography.labelSmall)
                                    Text("$selectedDay", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                            DropdownMenu(
                                expanded = dayExpanded,
                                onDismissRequest = { dayExpanded = false }
                            ) {
                                (1..maxDays).forEach { dy ->
                                    DropdownMenuItem(
                                        text = { Text("$dy") },
                                        onClick = {
                                            selectedDay = dy
                                            dayExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Image Upload Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            "Cargo Visual Register",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (cargoImagePath != null && File(cargoImagePath!!).exists()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(File(cargoImagePath!!)),
                                contentDescription = "Cargo Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .size(40.dp)
                                    .align(Alignment.TopEnd)
                                    .clickable { cargoImagePath = null }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "✖",
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .border(
                                    2.dp,
                                    MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(16.dp)
                                )
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                    RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "No photo registered",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Camera Button
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            modifier = Modifier.weight(1f).testTag("capture_camera_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Camera", fontWeight = FontWeight.Bold)
                        }

                        // Gallery Button
                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f).testTag("select_gallery_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gallery", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Notes Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Additional Notes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes & Shipping Details") },
                        placeholder = { Text("e.g. Fragile cargo, keep away from heat, call receiver on arrival...") },
                        leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("notes_input"),
                        shape = RoundedCornerShape(16.dp),
                        maxLines = 5
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Save Button
            Button(
                onClick = {
                    if (cargoDescription.isBlank()) {
                        Toast.makeText(context, "Please enter a cargo description", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (senderName.isBlank()) {
                        Toast.makeText(context, "Please enter sender name", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (receiverName.isBlank()) {
                        Toast.makeText(context, "Please enter receiver name", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (sentBy.isBlank()) {
                        Toast.makeText(context, "Please enter transport method / sent by", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    viewModel.insertShipment(
                        cargoDescription = cargoDescription,
                        senderName = senderName,
                        receiverName = receiverName,
                        sentBy = sentBy,
                        jalaliYear = selectedYear,
                        jalaliMonth = selectedMonth,
                        jalaliDay = selectedDay,
                        notes = notes,
                        imagePath = cargoImagePath
                    ) { newId ->
                        Toast.makeText(context, "Cargo Shipment registered successfully!", Toast.LENGTH_LONG).show()
                        onShipmentSaved()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_shipment_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Register Cargo Shipment", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
