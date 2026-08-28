package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ShipmentViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: ShipmentViewModel = hiltViewModel()
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "dashboard",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // 1. Dashboard Screen
                        composable("dashboard") {
                            DashboardScreen(
                                viewModel = viewModel,
                                onMonthSelected = { year, month ->
                                    navController.navigate("monthly_view/$year/$month")
                                },
                                onAddShipment = { year, month, day ->
                                    navController.navigate("add_shipment?year=$year&month=$month&day=$day")
                                }
                            )
                        }

                        // 2. Monthly View Screen
                        composable(
                            route = "monthly_view/{year}/{month}",
                            arguments = listOf(
                                navArgument("year") { type = NavType.IntType },
                                navArgument("month") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val year = backStackEntry.arguments?.getInt("year") ?: 1405
                            val month = backStackEntry.arguments?.getInt("month") ?: 1
                            MonthlyViewScreen(
                                viewModel = viewModel,
                                year = year,
                                month = month,
                                onBack = { navController.popBackStack() },
                                onDaySelected = { y, m, d ->
                                    navController.navigate("daily_view/$y/$m/$d")
                                },
                                onShipmentSelected = { id ->
                                    navController.navigate("shipment_details/$id")
                                },
                                onAddShipmentForDay = { y, m, d ->
                                    navController.navigate("add_shipment?year=$y&month=$m&day=$d")
                                }
                            )
                        }

                        // 3. Daily Shipments Screen
                        composable(
                            route = "daily_view/{year}/{month}/{day}",
                            arguments = listOf(
                                navArgument("year") { type = NavType.IntType },
                                navArgument("month") { type = NavType.IntType },
                                navArgument("day") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val year = backStackEntry.arguments?.getInt("year") ?: 1405
                            val month = backStackEntry.arguments?.getInt("month") ?: 1
                            val day = backStackEntry.arguments?.getInt("day") ?: 1
                            DailyShipmentsScreen(
                                viewModel = viewModel,
                                year = year,
                                month = month,
                                day = day,
                                onBack = { navController.popBackStack() },
                                onShipmentSelected = { id ->
                                    navController.navigate("shipment_details/$id")
                                },
                                onAddShipment = { y, m, d ->
                                    navController.navigate("add_shipment?year=$y&month=$m&day=$d")
                                }
                            )
                        }

                        // 4. Add Shipment Screen
                        composable(
                            route = "add_shipment?year={year}&month={month}&day={day}",
                            arguments = listOf(
                                navArgument("year") {
                                    type = NavType.IntType
                                    defaultValue = -1
                                },
                                navArgument("month") {
                                    type = NavType.IntType
                                    defaultValue = -1
                                },
                                navArgument("day") {
                                    type = NavType.IntType
                                    defaultValue = -1
                                }
                            )
                        ) { backStackEntry ->
                            val y = backStackEntry.arguments?.getInt("year").takeIf { it != -1 }
                            val m = backStackEntry.arguments?.getInt("month").takeIf { it != -1 }
                            val d = backStackEntry.arguments?.getInt("day").takeIf { it != -1 }
                            AddShipmentScreen(
                                viewModel = viewModel,
                                prefilledYear = y,
                                prefilledMonth = m,
                                prefilledDay = d,
                                onBack = { navController.popBackStack() },
                                onShipmentSaved = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 5. Shipment Details Screen
                        composable(
                            route = "shipment_details/{id}",
                            arguments = listOf(
                                navArgument("id") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val id = backStackEntry.arguments?.getInt("id") ?: 0
                            ShipmentDetailsScreen(
                                viewModel = viewModel,
                                shipmentId = id,
                                onBack = { navController.popBackStack() },
                                onDeleted = { navController.popBackStack() },
                                onEdit = { shipmentId ->
                                    navController.navigate("edit_shipment/$shipmentId")
                                }
                            )
                        }

                        // 6. Edit Shipment Screen
                        composable(
                            route = "edit_shipment/{id}",
                            arguments = listOf(
                                navArgument("id") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val id = backStackEntry.arguments?.getInt("id") ?: 0
                            EditShipmentScreen(
                                viewModel = viewModel,
                                shipmentId = id,
                                onBack = { navController.popBackStack() },
                                onShipmentUpdated = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
