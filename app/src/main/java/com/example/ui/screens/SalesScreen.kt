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
import com.example.data.model.SaleItem
import com.example.data.model.SaleLog
import com.example.ui.components.StylizedCard
import com.example.ui.components.StylizedTextField
import com.example.ui.viewmodel.SweetStockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    viewModel: SweetStockViewModel,
    onBack: () -> Unit
) {
    val saleLogs by viewModel.saleLogs.collectAsState()
    val inventory by viewModel.inventory.collectAsState()
    val finishedSweets = inventory.filter { !it.isRawMaterial }
    val shopConfig by viewModel.shopConfig.collectAsState()
    val currency = shopConfig?.currency ?: "₹"

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedInvoiceForReceipt by remember { mutableStateOf<SaleLog?>(null) }

    // Form states
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var invoiceNumber by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("0") }
    var gst by remember { mutableStateOf("0") }
    var paymentMethod by remember { mutableStateOf("Cash") }

    // Checkout list of items
    val checkoutItems = remember { mutableStateListOf<SaleItem>() }

    // Checkout selection helpers
    var selectedSweetProduct by remember { mutableStateOf<InventoryItem?>(null) }
    var sellQty by remember { mutableStateOf("") }
    var sellRate by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout & Invoices", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        customerName = ""
                        customerPhone = ""
                        invoiceNumber = "TXN-" + (100000..999999).random().toString()
                        discount = "0"
                        gst = "0"
                        paymentMethod = "Cash"
                        checkoutItems.clear()
                        showAddDialog = true
                    }) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = "New Sale")
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
                text = "Sales & Invoices Ledger",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            if (saleLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No sales invoices logged. Tap + to check out customers.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(saleLogs) { log ->
                        StylizedCard(
                            modifier = Modifier.clickable {
                                selectedInvoiceForReceipt = log
                            }
                        ) {
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
                                            text = "Customer: ${log.customerName.ifEmpty { "Counter Client" }}",
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
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Date: ${log.date}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("Margin: $currency${log.netProfit.toInt()}") }
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
            title = { Text("Log New Sales Checkout") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StylizedTextField(value = customerName, onValueChange = { customerName = it }, label = "Customer Name")
                    StylizedTextField(value = customerPhone, onValueChange = { customerPhone = it }, label = "Customer Contact Number")
                    StylizedTextField(value = invoiceNumber, onValueChange = { invoiceNumber = it }, label = "Invoice/Bill Number")

                    Text("Select Payment Mode", fontWeight = FontWeight.Bold)
                    val methods = listOf("Cash", "UPI", "Card", "Bank", "Credit")
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

                    Text("Checkout Sweets List", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    if (finishedSweets.isNotEmpty()) {
                        var expandedSweetSelect by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(onClick = { expandedSweetSelect = true }) {
                                Text(selectedSweetProduct?.name ?: "Select Sweet Product")
                            }
                            DropdownMenu(expanded = expandedSweetSelect, onDismissRequest = { expandedSweetSelect = false }) {
                                finishedSweets.forEach { sweet ->
                                    DropdownMenuItem(
                                        text = { Text("${sweet.name} (${sweet.quantity} ${sweet.unit} in stock)") },
                                        onClick = {
                                            selectedSweetProduct = sweet
                                            sellRate = sweet.sellingPrice.toString()
                                            expandedSweetSelect = false
                                        }
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StylizedTextField(value = sellQty, onValueChange = { sellQty = it }, label = "Qty to sell", modifier = Modifier.weight(1f))
                            StylizedTextField(value = sellRate, onValueChange = { sellRate = it }, label = "Rate ($currency)", modifier = Modifier.weight(1f))
                        }

                        Button(
                            onClick = {
                                val product = selectedSweetProduct
                                val qty = sellQty.toDoubleOrNull() ?: 0.0
                                val rate = sellRate.toDoubleOrNull() ?: 0.0
                                if (product != null && qty > 0.0 && rate > 0.0) {
                                    checkoutItems.add(
                                        SaleItem(
                                            id = product.id,
                                            name = product.name,
                                            quantity = qty,
                                            rate = rate,
                                            discountPercent = 0.0,
                                            gstPercent = product.gstPercent,
                                            totalCost = qty * product.pricePerUnit, // COGS basis (cost price)
                                            totalSale = qty * rate // gross selling
                                        )
                                    )
                                    selectedSweetProduct = null
                                    sellQty = ""
                                    sellRate = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Add Sweet Product")
                        }
                    } else {
                        Text("No finished sweets in stock. Create production logs first.", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }

                    // Display added checkout items list
                    checkoutItems.forEachIndexed { idx, sItem ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${sItem.name}: ${sItem.quantity} @ $currency${sItem.rate}")
                            IconButton(onClick = { checkoutItems.removeAt(idx) }) {
                                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    Text("Invoice Adjustments", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    StylizedTextField(value = discount, onValueChange = { discount = it }, label = "Manual Discount Amount ($currency)")
                    StylizedTextField(value = gst, onValueChange = { gst = it }, label = "Tax Rate/GST amount")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (checkoutItems.isNotEmpty()) {
                            viewModel.logSale(
                                invoiceNumber = invoiceNumber,
                                customerName = customerName,
                                customerPhone = customerPhone,
                                items = checkoutItems,
                                discount = discount.toDoubleOrNull() ?: 0.0,
                                gst = gst.toDoubleOrNull() ?: 0.0,
                                paymentMethod = paymentMethod
                            )
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Checkout Client")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Invoice Receipt Popup details
    selectedInvoiceForReceipt?.let { log ->
        val converters = com.example.data.model.Converters()
        val itemsList = converters.toSaleItemList(log.itemsJson)

        AlertDialog(
            onDismissRequest = { selectedInvoiceForReceipt = null },
            title = { Text("Invoice Receipt Summary") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Bill Number: ${log.invoiceNumber}", fontWeight = FontWeight.Bold)
                    Text("Date: ${log.date}")
                    Text("Customer: ${log.customerName.ifEmpty { "Cash Customer" }}")
                    Text("Contact: ${log.customerPhone.ifEmpty { "N/A" }}")
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Text("Purchased Sweets:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    itemsList.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.name} (${item.quantity})")
                            Text("$currency${item.totalSale.toInt()}")
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Discount:")
                        Text("-$currency${log.discount.toInt()}")
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("GST/Tax:")
                        Text("+$currency${log.gst.toInt()}")
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Grand Total:", fontWeight = FontWeight.Bold)
                        Text("$currency${log.grandTotal.toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Payment Mode:")
                        Text(log.paymentMethod, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedInvoiceForReceipt = null }) {
                    Text("Done")
                }
            }
        )
    }
}
