package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.components.StylizedCard
import com.example.ui.components.StylizedTextField
import com.example.ui.viewmodel.SweetStockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: SweetStockViewModel,
    onBack: () -> Unit
) {
    val inventory by viewModel.inventory.collectAsState()
    val shopConfig by viewModel.shopConfig.collectAsState()
    val currency = shopConfig?.currency ?: "₹"

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("All") } // All, Raw, Finished

    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<InventoryItem?>(null) }

    // Form states
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Raw Material") }
    var quantity by remember { mutableStateOf("") }
    var pricePerUnit by remember { mutableStateOf("") }
    var minStock by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("Kg") }
    var isRawMaterial by remember { mutableStateOf(true) }
    var sellingPrice by remember { mutableStateOf("") }
    var storageLocation by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val filteredList = inventory.filter {
        it.name.contains(searchQuery, ignoreCase = true) &&
        when (selectedTab) {
            "Raw" -> it.isRawMaterial
            "Finished" -> !it.isRawMaterial
            else -> true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        editingItem = null
                        name = ""
                        category = "Raw Material"
                        quantity = ""
                        pricePerUnit = ""
                        minStock = ""
                        unit = "Kg"
                        isRawMaterial = true
                        sellingPrice = ""
                        storageLocation = ""
                        notes = ""
                        showAddDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Item")
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
            // Search Bar
            StylizedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search inventory...",
                testTag = "inventory_search",
                leadingIcon = { Icon(Icons.Default.Search, null) }
            )

            // Filtering Tab Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Raw", "Finished").forEach { tab ->
                    FilterChip(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        label = { Text(tab) }
                    )
                }
            }

            // Inventory items list
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No inventory items found.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList) { item ->
                        StylizedCard(
                            modifier = Modifier.clickable {
                                editingItem = item
                                name = item.name
                                category = item.category
                                quantity = item.quantity.toString()
                                pricePerUnit = item.pricePerUnit.toString()
                                minStock = item.minStock.toString()
                                unit = item.unit
                                isRawMaterial = item.isRawMaterial
                                sellingPrice = item.sellingPrice.toString()
                                storageLocation = item.storageLocation
                                notes = item.notes
                                showAddDialog = true
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.5f)) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        AssistChip(
                                            onClick = {},
                                            label = { Text(if (item.isRawMaterial) "Raw Material" else "Finished Product") }
                                        )
                                        if (item.storageLocation.isNotEmpty()) {
                                            Text(
                                                text = "Loc: ${item.storageLocation}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.align(Alignment.CenterVertically)
                                            )
                                        }
                                    }
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "${item.quantity} ${item.unit}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.quantity <= item.minStock) Color.Red else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Cost: $currency${item.pricePerUnit}/${item.unit}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (!item.isRawMaterial) {
                                        Text(
                                            text = "Sell: $currency${item.sellingPrice}/${item.unit}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
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
            title = { Text(if (editingItem == null) "Add Inventory Item" else "Edit Inventory Item") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StylizedTextField(value = name, onValueChange = { name = it }, label = "Item Name *")

                    // Raw vs Finished select
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Is this a Raw Material?", fontWeight = FontWeight.Bold)
                        Switch(checked = isRawMaterial, onCheckedChange = { isRawMaterial = it })
                    }

                    StylizedTextField(value = category, onValueChange = { category = it }, label = "Category (e.g. Dairy, Sweet)")
                    StylizedTextField(value = quantity, onValueChange = { quantity = it }, label = "Current Stock Quantity *")
                    StylizedTextField(value = unit, onValueChange = { unit = it }, label = "Unit (Kg, Gram, Liter, Piece, etc.)")
                    StylizedTextField(value = pricePerUnit, onValueChange = { pricePerUnit = it }, label = "Price Per Unit ($currency) *")

                    if (!isRawMaterial) {
                        StylizedTextField(value = sellingPrice, onValueChange = { sellingPrice = it }, label = "Selling Price ($currency) *")
                    }

                    StylizedTextField(value = minStock, onValueChange = { minStock = it }, label = "Min Stock Alert Threshold *")
                    StylizedTextField(value = storageLocation, onValueChange = { storageLocation = it }, label = "Storage Location (e.g. Shelf A)")
                    StylizedTextField(value = notes, onValueChange = { notes = it }, label = "Notes", singleLine = false)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qtyVal = quantity.toDoubleOrNull() ?: 0.0
                        val priceVal = pricePerUnit.toDoubleOrNull() ?: 0.0
                        val minVal = minStock.toDoubleOrNull() ?: 0.0
                        val sellVal = sellingPrice.toDoubleOrNull() ?: 0.0

                        if (name.isNotEmpty() && quantity.isNotEmpty() && pricePerUnit.isNotEmpty()) {
                            val itemToSave = InventoryItem(
                                id = editingItem?.id ?: 0,
                                name = name,
                                category = category,
                                quantity = qtyVal,
                                unit = unit,
                                pricePerUnit = priceVal,
                                minStock = minVal,
                                isRawMaterial = isRawMaterial,
                                sellingPrice = if (isRawMaterial) 0.0 else sellVal,
                                storageLocation = storageLocation,
                                notes = notes,
                                totalCost = qtyVal * priceVal
                            )
                            viewModel.saveInventoryItem(itemToSave)
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Row {
                    if (editingItem != null) {
                        TextButton(
                            onClick = {
                                editingItem?.let { viewModel.deleteInventoryItem(it) }
                                showAddDialog = false
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete")
                        }
                    }
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}
