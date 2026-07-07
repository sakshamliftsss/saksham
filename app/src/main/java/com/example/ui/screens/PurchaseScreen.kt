package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InventoryItem
import com.example.data.model.PurchaseItem
import com.example.ui.components.StylizedCard
import com.example.ui.components.StylizedTextField
import com.example.ui.viewmodel.SweetStockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseScreen(
    viewModel: SweetStockViewModel,
    onBack: () -> Unit
) {
    val purchaseLogs by viewModel.purchaseLogs.collectAsState()
    val inventory by viewModel.inventory.collectAsState()
    val rawMaterials = inventory.filter { it.isRawMaterial }
    val shopConfig by viewModel.shopConfig.collectAsState()
    val currency = shopConfig?.currency ?: "₹"

    var showAddDialog by remember { mutableStateOf(false) }

    // Form states
    var supplierName by remember { mutableStateOf("") }
    var invoiceNumber by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("0") }
    var transport by remember { mutableStateOf("0") }
    var loading by remember { mutableStateOf("0") }
    var otherCharges by remember { mutableStateOf("0") }
    var paymentMethod by remember { mutableStateOf("Cash") }

    // Selected items list
    val purchaseItems = remember { mutableStateListOf<PurchaseItem>() }

    // Add item helper form
    var selectedRawMaterial by remember { mutableStateOf<InventoryItem?>(null) }
    var purchaseQty by remember { mutableStateOf("") }
    var purchaseRate by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Raw Material Purchases", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        supplierName = ""
                        invoiceNumber = "INV-" + (1000..9999).random().toString()
                        discount = "0"
                        transport = "0"
                        loading = "0"
                        otherCharges = "0"
                        paymentMethod = "Cash"
                        purchaseItems.clear()
                        showAddDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Log Purchase")
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Purchase Transactions Ledger",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            if (purchaseLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No purchases recorded yet. Tap + to log raw materials.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(purchaseLogs) { log ->
                        StylizedCard {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Inv: ${log.invoiceNumber}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Supplier: ${log.supplierName.ifEmpty { "Cash Purchase" }}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "$currency${log.grandTotal.toInt()}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Date: ${log.purchaseDate}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("Method: ${log.paymentMethod}") }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Log Raw Material Purchase") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StylizedTextField(value = supplierName, onValueChange = { supplierName = it }, label = "Supplier Name *")
                    StylizedTextField(value = invoiceNumber, onValueChange = { invoiceNumber = it }, label = "Invoice Number")

                    // Payment Method selector
                    Text("Payment Mode", fontWeight = FontWeight.Bold)
                    val methods = listOf("Cash", "UPI", "Bank", "Credit")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        methods.forEach { m ->
                            FilterChip(
                                selected = paymentMethod == m,
                                onClick = { paymentMethod = m },
                                label = { Text(m) }
                            )
                        }
                    }

                    Text("Add Purchased Items", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    if (rawMaterials.isNotEmpty()) {
                        var expandedRawSelect by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(onClick = { expandedRawSelect = true }) {
                                Text(selectedRawMaterial?.name ?: "Select Raw Material")
                            }
                            DropdownMenu(expanded = expandedRawSelect, onDismissRequest = { expandedRawSelect = false }) {
                                rawMaterials.forEach { mat ->
                                    DropdownMenuItem(
                                        text = { Text("${mat.name} (${mat.unit})") },
                                        onClick = {
                                            selectedRawMaterial = mat
                                            expandedRawSelect = false
                                        }
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StylizedTextField(value = purchaseQty, onValueChange = { purchaseQty = it }, label = "Qty", modifier = Modifier.weight(1f))
                            StylizedTextField(value = purchaseRate, onValueChange = { purchaseRate = it }, label = "Rate ($currency)", modifier = Modifier.weight(1f))
                        }

                        Button(
                            onClick = {
                                val mat = selectedRawMaterial
                                val qty = purchaseQty.toDoubleOrNull() ?: 0.0
                                val rate = purchaseRate.toDoubleOrNull() ?: 0.0
                                if (mat != null && qty > 0.0 && rate > 0.0) {
                                    purchaseItems.add(
                                        PurchaseItem(
                                            id = mat.id,
                                            name = mat.name,
                                            quantity = qty,
                                            unit = mat.unit,
                                            pricePerUnit = rate,
                                            gstPercent = mat.gstPercent,
                                            totalCost = qty * rate
                                        )
                                    )
                                    selectedRawMaterial = null
                                    purchaseQty = ""
                                    purchaseRate = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Add Item")
                        }
                    } else {
                        Text("No Raw Materials in database. Create them in Inventory first.", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }

                    // Display added items list
                    purchaseItems.forEachIndexed { index, pItem ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${pItem.name}: ${pItem.quantity} ${pItem.unit} @ $currency${pItem.pricePerUnit}")
                            IconButton(onClick = { purchaseItems.removeAt(index) }) {
                                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    Text("Other Additional Logistics", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    StylizedTextField(value = discount, onValueChange = { discount = it }, label = "Overall Discount ($currency)")
                    StylizedTextField(value = transport, onValueChange = { transport = it }, label = "Transport & Delivery cost")
                    StylizedTextField(value = loading, onValueChange = { loading = it }, label = "Loading charges")
                    StylizedTextField(value = otherCharges, onValueChange = { otherCharges = it }, label = "Other miscellaneous costs")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (supplierName.isNotEmpty() && purchaseItems.isNotEmpty()) {
                            viewModel.logPurchase(
                                supplierName = supplierName,
                                invoiceNumber = invoiceNumber,
                                items = purchaseItems,
                                discount = discount.toDoubleOrNull() ?: 0.0,
                                transport = transport.toDoubleOrNull() ?: 0.0,
                                loading = loading.toDoubleOrNull() ?: 0.0,
                                other = otherCharges.toDoubleOrNull() ?: 0.0,
                                paymentMethod = paymentMethod
                            )
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Record Transaction")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
