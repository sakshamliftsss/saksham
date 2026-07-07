package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SweetStockViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: SweetStockViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val shopConfig by viewModel.shopConfig.collectAsState()
            val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()

            MyApplicationTheme(darkTheme = isDarkMode) {
                if (shopConfig == null) {
                    // Loading state
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
                    }
                } else if (!shopConfig!!.isSetupComplete) {
                    // Show Welcome Setup Wizard if not completed
                    WelcomeSetupScreen(viewModel = viewModel) {
                        // After completing first time setup, navigate to lock terminal
                        viewModel.logout()
                    }
                } else if (!isUserLoggedIn) {
                    // Lock screen terminal gate
                    LoginScreen(viewModel = viewModel) {
                        // On successful authentication, unlock
                    }
                } else {
                    // Authenticated Main Business workspace with persistent Bottom Navigation
                    MainWorkspace(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainWorkspace(viewModel: SweetStockViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Tab list for Bottom Navigation
    val tabs = listOf(
        Triple("Dashboard", "dashboard", Icons.Default.Dashboard),
        Triple("Inventory", "inventory", Icons.Default.Inventory),
        Triple("Recipes", "recipes", Icons.Default.Receipt),
        Triple("Analytics", "analytics", Icons.Default.Analytics),
        Triple("AI Assistant", "ai_assistant", Icons.Default.AutoAwesome)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Only show Bottom bar on the core tab screens to keep layout clean
            val showBottomBar = tabs.any { it.second == currentRoute }
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            icon = { Icon(tab.third, contentDescription = tab.first) },
                            label = { Text(tab.first) },
                            selected = currentRoute == tab.second,
                            onClick = {
                                if (currentRoute != tab.second) {
                                    navController.navigate(tab.second) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                DashboardScreen(viewModel = viewModel) { targetRoute ->
                    navController.navigate(targetRoute)
                }
            }
            composable("inventory") {
                InventoryScreen(viewModel = viewModel) {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = false }
                    }
                }
            }
            composable("recipes") {
                RecipeScreen(viewModel = viewModel) {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = false }
                    }
                }
            }
            composable("analytics") {
                AnalyticsScreen(viewModel = viewModel) {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = false }
                    }
                }
            }
            composable("ai_assistant") {
                AssistantScreen(viewModel = viewModel) {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = false }
                    }
                }
            }
            composable("purchase") {
                PurchaseScreen(viewModel = viewModel) {
                    navController.popBackStack()
                }
            }
            composable("sales") {
                SalesScreen(viewModel = viewModel) {
                    navController.popBackStack()
                }
            }
            composable("settings") {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onLogout = { /* Will trigger MainActivity recomposition to Login Screen */ }
                )
            }
        }
    }
}
