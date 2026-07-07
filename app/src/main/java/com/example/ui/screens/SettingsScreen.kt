package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShopConfig
import com.example.ui.components.PrimaryActionButton
import com.example.ui.components.StylizedCard
import com.example.ui.components.StylizedTextField
import com.example.ui.viewmodel.SweetStockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SweetStockViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val shopConfig by viewModel.shopConfig.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()

    val scrollState = rememberScrollState()

    // Shop settings states
    var shopName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("") }

    // Onboarding values loaded
    LaunchedEffect(shopConfig) {
        shopConfig?.let {
            shopName = it.shopName
            ownerName = it.ownerName
            currency = it.currency
            pinCode = it.pinCode
        }
    }

    // Activity Result Launcher to select local JSON files for restoration
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val result = viewModel.restoreDatabase(context, uri)
            Toast.makeText(context, result, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Configurations", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dark Mode / Language
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            StylizedCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DarkMode, null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Dark Mode Theme")
                    }
                    Switch(checked = isDarkMode, onCheckedChange = { viewModel.toggleDarkMode(it) })
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Display Language")
                    }
                    Text(selectedLanguage, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            // Bento Grid Customization
            val bentoOrder by viewModel.bentoCardsOrder.collectAsState()
            val bentoVisibility by viewModel.bentoCardsVisibility.collectAsState()

            Text(
                text = "Dashboard Layout (Bento Grid)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            StylizedCard {
                Text(
                    text = "Configure the display order and toggle visibility of specific cards on your dashboard.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                bentoOrder.forEachIndexed { index, cardId ->
                    val isVisible = cardId in bentoVisibility
                    val label = when (cardId) {
                        "today_sales" -> "Today's Sales Banner"
                        "net_profit" -> "Today's Net Profit"
                        "expenses" -> "Today's Expenses"
                        "inventory_status" -> "Inventory Status"
                        "low_stock" -> "Low Stock Warnings"
                        "smart_forecast" -> "Smart Forecast (AI)"
                        "quick_actions" -> "Quick Operations"
                        "monthly_sales" -> "Monthly Gross Sales"
                        "monthly_profit" -> "Monthly Net Profit"
                        "customer_unpaid" -> "Customer Unpaid Balance"
                        "supplier_unpaid" -> "Supplier Unpaid Balance"
                        "sales_channels" -> "Sales Channels Summary"
                        "best_least_sellers" -> "Top & Least Sellers"
                        else -> cardId
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Up / Down arrows for ordering
                            IconButton(
                                onClick = {
                                    if (index > 0) {
                                        val newOrder = bentoOrder.toMutableList()
                                        val temp = newOrder[index]
                                        newOrder[index] = newOrder[index - 1]
                                        newOrder[index - 1] = temp
                                        viewModel.updateBentoCardsOrder(newOrder)
                                    }
                                },
                                enabled = index > 0,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Move Up",
                                    tint = if (index > 0) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    if (index < bentoOrder.size - 1) {
                                        val newOrder = bentoOrder.toMutableList()
                                        val temp = newOrder[index]
                                        newOrder[index] = newOrder[index + 1]
                                        newOrder[index + 1] = temp
                                        viewModel.updateBentoCardsOrder(newOrder)
                                    }
                                },
                                enabled = index < bentoOrder.size - 1,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = "Move Down",
                                    tint = if (index < bentoOrder.size - 1) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // Visibility toggle
                        Switch(
                            checked = isVisible,
                            onCheckedChange = { checked ->
                                val newVis = bentoVisibility.toMutableSet()
                                if (checked) {
                                    newVis.add(cardId)
                                } else {
                                    newVis.remove(cardId)
                                }
                                viewModel.updateBentoCardsVisibility(newVis)
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    if (index < bentoOrder.size - 1) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), thickness = 1.dp)
                    }
                }
            }

            // Edit Profile Info
            Text(
                text = "Shop profile Info",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            StylizedCard {
                StylizedTextField(value = shopName, onValueChange = { shopName = it }, label = "Shop Name")
                Spacer(modifier = Modifier.height(12.dp))
                StylizedTextField(value = ownerName, onValueChange = { ownerName = it }, label = "Owner/Authorized Person")
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StylizedTextField(value = currency, onValueChange = { currency = it }, label = "Currency Symbol", modifier = Modifier.weight(1f))
                    StylizedTextField(value = pinCode, onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinCode = it }, label = "Security PIN", modifier = Modifier.weight(1.5f))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        shopConfig?.let {
                            viewModel.updateShopInfo(
                                it.copy(
                                    shopName = shopName,
                                    ownerName = ownerName,
                                    currency = currency,
                                    pinCode = pinCode
                                )
                            )
                            Toast.makeText(context, "Shop configuration saved successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Configurations")
                }
            }

            // Database Operations (Backup & Restore)
            Text(
                text = "Database & Sync",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            StylizedCard {
                Text(
                    text = "Restore database from an existing SweetStock JSON file. This overrides all current records.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { restoreLauncher.launch("application/json") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Icon(Icons.Default.UploadFile, null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Restore Backup (JSON)")
                }
            }

            // Logout Option
            Button(
                onClick = {
                    viewModel.logout()
                    onLogout()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(Icons.Default.ExitToApp, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout / Lock Console")
            }
        }
    }
}
