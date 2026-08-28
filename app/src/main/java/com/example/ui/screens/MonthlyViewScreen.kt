package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.entity.Shipment
import com.example.ui.viewmodel.ShipmentViewModel
import com.example.util.JalaliCalendar
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyViewScreen(
    viewModel: ShipmentViewModel,
    year: Int,
    month: Int,
    onBack: () -> Unit,
    onDaySelected: (year: Int, month: Int, day: Int) -> Unit,
    onShipmentSelected: (id: Int) -> Unit,
    onAddShipmentForDay: (year: Int, month: Int, day: Int) -> Unit
) {
    val context = LocalContext.current
    val allShipments by viewModel.allShipments.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredShipments by viewModel.filteredShipments.collectAsState()
    var showExportMenu by remember { mutableStateOf(false) }

    // Filter shipments for this specific year and month (using search if active)
    val shipmentsThisMonth = remember(allShipments, filteredShipments, year, month, searchQuery) {
        if (searchQuery.isNotBlank()) {
            filteredShipments.filter { it.jalaliYear == year && it.jalaliMonth == month }
        } else {
            allShipments.filter { it.jalaliYear == year && it.jalaliMonth == month }
        }
    }

    // Determine number of days in this month
    val maxDays = remember(year, month) {
        when (month) {
            in 1..6 -> 31
            in 7..11 -> 30
            12 -> {
                val isLeap = (year % 33) in intArrayOf(1, 5, 9, 13, 17, 22, 26, 30)
                if (isLeap) 30 else 29
            }
            else -> 30
        }
    }

    // Group shipments by day
    val shipmentsByDay = remember(shipmentsThisMonth) {
        shipmentsThisMonth.groupBy { it.jalaliDay }
    }

    val monthName = remember(month) { JalaliCalendar.getJalaliMonthName(month) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = monthName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Year $year • ${shipmentsThisMonth.size} Shipments",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Export button
                    Box {
                        IconButton(onClick = { showExportMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Export",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export to Excel (.xlsx)") },
                                onClick = {
                                    showExportMenu = false
                                    viewModel.exportToExcel(context, shipmentsThisMonth, "shipments_${year}_${month}")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export to PDF") },
                                onClick = {
                                    showExportMenu = false
                                    viewModel.exportToPdf(context, shipmentsThisMonth, "shipments_${year}_${month}")
                                }
                            )
                        }
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
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_bar"),
                placeholder = { Text("Search in this month...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearSearch() }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .testTag("monthly_days_list"),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items((1..maxDays).toList()) { day ->
                    val dayShipments = shipmentsByDay[day] ?: emptyList()
                    DayCard(
                        day = day,
                        monthName = JalaliCalendar.getJalaliMonthNameFa(month),
                        shipments = dayShipments,
                        onAddShipment = { onAddShipmentForDay(year, month, day) },
                        onViewDayDetails = { onDaySelected(year, month, day) },
                        onShipmentSelected = onShipmentSelected
                    )
                }
            }
        }
    }
}

@Composable
fun DayCard(
    day: Int,
    monthName: String,
    shipments: List<Shipment>,
    onAddShipment: () -> Unit,
    onViewDayDetails: () -> Unit,
    onShipmentSelected: (id: Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("day_card_$day"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (shipments.isNotEmpty()) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (shipments.isNotEmpty()) 2.dp else 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Day Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onViewDayDetails)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (shipments.isNotEmpty()) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$day",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (shipments.isNotEmpty()) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = "$day $monthName",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (shipments.isNotEmpty()) "${shipments.size} shipments registered" else "No shipments",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onAddShipment,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), CircleShape)
                            .size(36.dp)
                            .testTag("add_shipment_day_$day")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add shipment on day $day",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    if (shipments.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onViewDayDetails,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "View Day Shipments"
                            )
                        }
                    }
                }
            }

            // Shipments List inside Day Card
            if (shipments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    shipments.take(3).forEach { shipment ->
                        CompactShipmentRow(
                            shipment = shipment,
                            onClick = { onShipmentSelected(shipment.id) }
                        )
                    }
                    if (shipments.size > 3) {
                        TextButton(
                            onClick = onViewDayDetails,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                "Show all ${shipments.size} shipments",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompactShipmentRow(
    shipment: Shipment,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail image or shipping icon
        if (shipment.imagePath != null && File(shipment.imagePath).exists()) {
            Image(
                painter = rememberAsyncImagePainter(File(shipment.imagePath)),
                contentDescription = "Cargo Image",
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = "Logistics",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = shipment.cargoDescription,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${shipment.senderName} ➔ ${shipment.receiverName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            // Status badge
            StatusBadge(status = shipment.status)
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Details",
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun StatusBadge(status: String) {
    val (backgroundColor, textColor) = when (status) {
        Shipment.STATUS_DELIVERED -> Color(0xFF2E7D32).copy(alpha = 0.15f) to Color(0xFF2E7D32)
        Shipment.STATUS_RETURNED -> Color(0xFFC62828).copy(alpha = 0.15f) to Color(0xFFC62828)
        else -> Color(0xFF1565C0).copy(alpha = 0.15f) to Color(0xFF1565C0)
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = backgroundColor,
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
