package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DashboardStatCard
import com.example.ui.components.StylizedCard
import com.example.ui.components.ThemeGradients
import com.example.ui.viewmodel.SweetStockViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: SweetStockViewModel,
    onNavigateTo: (String) -> Unit
) {
    val shopConfig by viewModel.shopConfig.collectAsState()
    val inventory by viewModel.inventory.collectAsState()
    val saleLogs by viewModel.saleLogs.collectAsState()
    val purchaseLogs by viewModel.purchaseLogs.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val bentoCardsOrder by viewModel.bentoCardsOrder.collectAsState()
    val bentoCardsVisibility by viewModel.bentoCardsVisibility.collectAsState()

    val currency = shopConfig?.currency ?: "₹"
    val scrollState = rememberScrollState()

    // 1. Calculations for Today & Current Month
    val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val currentMonthStr = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

    // Sales calculations
    val todaySales = saleLogs.filter { it.date == todayDateStr }.sumOf { it.grandTotal }
    val todayCost = saleLogs.filter { it.date == todayDateStr }.sumOf { it.totalCost }
    val todayProfit = saleLogs.filter { it.date == todayDateStr }.sumOf { it.netProfit }

    val monthlySales = saleLogs.filter { it.date.startsWith(currentMonthStr) }.sumOf { it.grandTotal }
    val monthlyProfit = saleLogs.filter { it.date.startsWith(currentMonthStr) }.sumOf { it.netProfit }

    // Expense calculations
    val todayExpenses = expenses.filter { it.date == todayDateStr }.sumOf { it.amount }
    val monthlyExpenses = expenses.filter { it.date.startsWith(currentMonthStr) }.sumOf { it.amount }

    // Inventory Calculations
    val totalInventoryValue = inventory.sumOf { it.quantity * it.pricePerUnit }
    val lowStockCount = inventory.filter { it.quantity <= it.minStock }.size

    // Customer / Supplier Pending payments
    val customerPending = customers.sumOf { it.pendingBalance }
    val supplierPending = suppliers.sumOf { it.outstandingPayment }

    // Collections channels (aggregate all sales)
    val cashReceived = saleLogs.filter { it.paymentMethod.equals("Cash", ignoreCase = true) }.sumOf { it.grandTotal }
    val upiReceived = saleLogs.filter { it.paymentMethod.equals("UPI", ignoreCase = true) }.sumOf { it.grandTotal }
    val bankReceived = saleLogs.filter { it.paymentMethod.equals("Bank", ignoreCase = true) }.sumOf { it.grandTotal }
    val creditSales = saleLogs.filter { it.paymentMethod.equals("Credit", ignoreCase = true) }.sumOf { it.grandTotal }

    // Best/Least Selling Sweets (simplistic deduction from saleLogs)
    val sweetSalesCount = mutableMapOf<String, Double>()
    saleLogs.forEach { log ->
        val converters = com.example.data.model.Converters()
        val items = converters.toSaleItemList(log.itemsJson)
        items.forEach { item ->
            sweetSalesCount[item.name] = (sweetSalesCount[item.name] ?: 0.0) + item.quantity
        }
    }
    val bestSelling = sweetSalesCount.maxByOrNull { it.value }?.key ?: "N/A"
    val leastSelling = sweetSalesCount.minByOrNull { it.value }?.key ?: "N/A"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.sweet_minimalist_logo_1783418252258),
                                contentDescription = "Shop Logo",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Column {
                                Text(
                                    text = shopConfig?.shopName ?: "Royal Sweets",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    lineHeight = 20.sp,
                                    color = Color(0xFF1D1B20)
                                )
                                Text(
                                    text = "SweetStock Pro Dashboard",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF49454F),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        // User Profile Circle on the right
                        val initials = shopConfig?.ownerName?.split(" ")?.mapNotNull { it.firstOrNull()?.toString() }?.joinToString("")?.take(2)?.uppercase() ?: "JD"
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(19.dp))
                                .background(Color(0xFFEADDFF))
                                .border(2.dp, Color.White, RoundedCornerShape(19.dp))
                                .clickable { onNavigateTo("settings") },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                color = Color(0xFF21005D),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
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
        val rawMaterialPurchasesToday = purchaseLogs.filter { it.purchaseDate == todayDateStr }.sumOf { it.grandTotal }
        val rawMaterialStockValue = inventory.filter { it.isRawMaterial }.sumOf { it.quantity * it.pricePerUnit }
        val rawMaterialsCount = inventory.filter { it.isRawMaterial }.size
        val finishedSweetsCount = inventory.filter { !it.isRawMaterial }.size

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // One-Line Menu Options Row (Not scattered)
            Text(
                text = "Stock & Business Options",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val navOptions = listOf(
                    Triple("Inventory", Icons.Default.Inventory, "inventory"),
                    Triple("Purchase Raw", Icons.Default.AddShoppingCart, "purchase"),
                    Triple("Produce Sweets", Icons.Default.Receipt, "recipes"),
                    Triple("Sales Log", Icons.Default.ShoppingCart, "sales"),
                    Triple("Analytics", Icons.Default.Analytics, "analytics"),
                    Triple("AI Assistant", Icons.Default.AutoAwesome, "ai_assistant"),
                    Triple("Settings", Icons.Default.Settings, "settings")
                )
                navOptions.forEach { opt ->
                    Card(
                        modifier = Modifier
                            .clickable { onNavigateTo(opt.third) }
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = opt.second,
                                contentDescription = opt.first,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = opt.first,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Key Metrics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // 1. Profit Made Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFEADDFF), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Profit Made Today",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$currency${todayProfit.toInt()}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = "Profit Icon",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "This Month's Profit",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF49454F)
                            )
                            Text(
                                text = "$currency${monthlyProfit.toInt()}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF21005D)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFEADDFF), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Sales: $currency${monthlySales.toInt()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF21005D)
                            )
                        }
                    }
                }
            }

            // 2. Price for Raw Materials Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE7E0EC), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Price for Raw Materials Purchased Today",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF49454F),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$currency${rawMaterialPurchasesToday.toInt()}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1D1B20)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFFF3EDF7), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddShoppingCart,
                                contentDescription = "Raw Materials Icon",
                                tint = Color(0xFF49454F)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Raw Material Stock Value",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF49454F)
                            )
                            Text(
                                text = "$currency${rawMaterialStockValue.toInt()}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D1B20)
                            )
                        }
                        Button(
                            onClick = { onNavigateTo("purchase") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Buy Stock", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 3. Stock Used & Levels Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFF9DEDC), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF7FF)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Stock Levels & Status",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (lowStockCount > 0) Color(0xFFB3261E) else Color(0xFF49454F),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$lowStockCount Low stock items",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (lowStockCount > 0) Color(0xFFB3261E) else Color(0xFF1D1B20)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    if (lowStockCount > 0) Color(0xFFF9DEDC) else Color(0xFFE8DEF8),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory,
                                contentDescription = "Inventory Icon",
                                tint = if (lowStockCount > 0) Color(0xFFB3261E) else Color(0xFF6750A4)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Raw Materials",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF49454F)
                                )
                                Text(
                                    text = "$rawMaterialsCount items",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D1B20)
                                )
                            }
                            Column {
                                Text(
                                    text = "Finished Sweets",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF49454F)
                                )
                                Text(
                                    text = "$finishedSweetsCount items",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D1B20)
                                )
                            }
                        }
                        Button(
                            onClick = { onNavigateTo("inventory") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Manage", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RenderBentoCard(
    id: String,
    currency: String,
    todaySales: Double,
    todayProfit: Double,
    todayExpenses: Double,
    totalInventoryValue: Double,
    lowStockCount: Int,
    monthlySales: Double,
    monthlyProfit: Double,
    customerPending: Double,
    supplierPending: Double,
    cashReceived: Double,
    upiReceived: Double,
    bankReceived: Double,
    creditSales: Double,
    bestSelling: String,
    leastSelling: String,
    onNavigateTo: (String) -> Unit
) {
    when (id) {
        "today_sales" -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF6750A4), Color(0xFF4F378B))
                        )
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 10.dp, y = 10.dp)
                        .size(110.dp)
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(55.dp))
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TODAY'S SALES",
                            color = Color(0xFFEADDFF),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$currency${todaySales.toInt()}",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = "🍬",
                        fontSize = 32.sp
                    )
                }
            }
        }
        "net_profit" -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .border(1.dp, Color(0xFFE7E0EC), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8DEF8)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Net Profit",
                        color = Color(0xFF21005D),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "$currency${todayProfit.toInt()}",
                            color = Color(0xFF21005D),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "+8%",
                                color = Color(0xFF1D192B),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
        "expenses" -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Expenses",
                        color = Color(0xFF49454F),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$currency${todayExpenses.toInt()}",
                        color = Color(0xFF1D1B20),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        "inventory_status" -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .border(1.dp, Color(0xFFE7E0EC), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Inventory Status",
                        color = Color(0xFF49454F),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "In Stock Value",
                                fontSize = 11.sp,
                                color = Color(0xFF49454F)
                            )
                            Text(
                                text = "$currency${totalInventoryValue.toInt()}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D1B20)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val stockPct = if (totalInventoryValue > 0) 0.72f else 0f
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .background(Color(0xFFE7E0EC), RoundedCornerShape(3.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(stockPct)
                                    .fillMaxHeight()
                                    .background(Color(0xFF6750A4), RoundedCornerShape(3.dp))
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Value: $currency${(totalInventoryValue/1000).toInt()}K",
                            fontSize = 10.sp,
                            color = Color(0xFF49454F),
                            style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        )
                    }
                }
            }
        }
        "low_stock" -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clickable { onNavigateTo("inventory") }
                    .border(1.dp, Color(0xFFF9DEDC), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9DEDC)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFFB3261E), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Column {
                        Text(
                            text = "Low Stock",
                            color = Color(0xFF410E0B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$lowStockCount critical",
                            color = Color(0xFF410E0B),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
        "smart_forecast" -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clickable { onNavigateTo("ai_assistant") }
                    .border(1.dp, Color(0xFFEADDFF), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFF21005D), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AI",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        )
                    }
                    Column {
                        Text(
                            text = "Smart Forecast",
                            color = Color(0xFF21005D),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "AI Assistant",
                            color = Color(0xFF21005D),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
        "quick_actions" -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF7FF)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quick Operations",
                            color = Color(0xFF1D1B20),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "View All",
                            color = Color(0xFF6750A4),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onNavigateTo("settings") }
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val quickActions = listOf(
                            Triple("Sale", "➕", "sales"),
                            Triple("Purchase", "📦", "purchase"),
                            Triple("Produce", "🍳", "recipes"),
                            Triple("Report", "📊", "settings")
                        )
                        quickActions.forEach { action ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigateTo(action.third) },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color(0xFFE8DEF8), RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(action.second, fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = action.first,
                                    color = Color(0xFF49454F),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
        "monthly_sales" -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .border(1.dp, Color(0xFFE7E0EC), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Monthly Sales", style = MaterialTheme.typography.bodySmall, color = Color(0xFF49454F), fontWeight = FontWeight.Bold)
                    Text(
                        text = "$currency${monthlySales.toInt()}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D1B20)
                    )
                }
            }
        }
        "monthly_profit" -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .border(1.dp, Color(0xFFE7E0EC), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Monthly Profit", style = MaterialTheme.typography.bodySmall, color = Color(0xFF49454F), fontWeight = FontWeight.Bold)
                    Text(
                        text = "$currency${monthlyProfit.toInt()}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6750A4)
                    )
                }
            }
        }
        "customer_unpaid" -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .border(1.dp, Color(0xFFE7E0EC), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Customer Unpaid", style = MaterialTheme.typography.bodySmall, color = Color(0xFF49454F), fontWeight = FontWeight.Bold)
                    Text(
                        text = "$currency${customerPending.toInt()}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (customerPending > 0) Color(0xFFB3261E) else Color(0xFF1D1B20)
                    )
                }
            }
        }
        "supplier_unpaid" -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .border(1.dp, Color(0xFFE7E0EC), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Supplier Unpaid", style = MaterialTheme.typography.bodySmall, color = Color(0xFF49454F), fontWeight = FontWeight.Bold)
                    Text(
                        text = "$currency${supplierPending.toInt()}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (supplierPending > 0) Color(0xFFFF9800) else Color(0xFF1D1B20)
                    )
                }
            }
        }
        "sales_channels" -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE7E0EC), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Sales Channels Summary",
                        color = Color(0xFF1D1B20),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Cash", style = MaterialTheme.typography.bodySmall, color = Color(0xFF49454F))
                            Text("$currency${cashReceived.toInt()}", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20))
                        }
                        Column {
                            Text("UPI", style = MaterialTheme.typography.bodySmall, color = Color(0xFF49454F))
                            Text("$currency${upiReceived.toInt()}", fontWeight = FontWeight.Bold, color = Color(0xFF6750A4))
                        }
                        Column {
                            Text("Bank", style = MaterialTheme.typography.bodySmall, color = Color(0xFF49454F))
                            Text("$currency${bankReceived.toInt()}", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20))
                        }
                        Column {
                            Text("Credit", style = MaterialTheme.typography.bodySmall, color = Color(0xFF49454F))
                            Text("$currency${creditSales.toInt()}", fontWeight = FontWeight.Bold, color = Color(0xFFB3261E))
                        }
                    }
                }
            }
        }
        "best_least_sellers" -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE7E0EC), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Top Selling Sweet", style = MaterialTheme.typography.bodySmall, color = Color(0xFF49454F), fontWeight = FontWeight.Bold)
                        Text(bestSelling, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF6750A4))
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(Color(0xFFE7E0EC))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Least Selling Sweet", style = MaterialTheme.typography.bodySmall, color = Color(0xFF49454F), fontWeight = FontWeight.Bold)
                        Text(leastSelling, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFB3261E))
                    }
                }
            }
        }
    }
}
