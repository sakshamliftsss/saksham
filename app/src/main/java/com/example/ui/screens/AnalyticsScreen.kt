package com.example.ui.screens

import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DonutChart
import com.example.ui.components.InteractiveTrendGraph
import com.example.ui.components.StylizedCard
import com.example.ui.viewmodel.SweetStockViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: SweetStockViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val shopConfig by viewModel.shopConfig.collectAsState()
    val saleLogs by viewModel.saleLogs.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val inventory by viewModel.inventory.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()

    val currency = shopConfig?.currency ?: "₹"
    val scrollState = rememberScrollState()

    // 1. Calculations
    val totalSales = saleLogs.sumOf { it.grandTotal }
    val totalExpenses = expenses.sumOf { it.amount }
    val totalProfits = saleLogs.sumOf { it.netProfit }
    val marginPercentage = if (totalSales > 0) (totalProfits / totalSales) * 100 else 0.0

    // Expense breakdown by categories
    val expenseCategories = expenses.groupBy { it.category }.map { (cat, list) ->
        cat to list.sumOf { it.amount }
    }.sortedByDescending { it.second }

    // Sales Trend (dummy points if empty, else aggregate values)
    val salesTrendPoints = if (saleLogs.isEmpty()) {
        listOf(1000.0, 2500.0, 1800.0, 3200.0, 4100.0, 3800.0, 5200.0)
    } else {
        saleLogs.take(10).map { it.grandTotal }.reversed()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Business Analytics", fontWeight = FontWeight.Bold) },
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
            // Summary Banner
            StylizedCard(
                borderColor = MaterialTheme.colorScheme.primary,
                backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "Financial Overview Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Gross Revenue", style = MaterialTheme.typography.bodySmall)
                        Text("$currency${totalSales.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    }
                    Column {
                        Text("Total Expense", style = MaterialTheme.typography.bodySmall)
                        Text("$currency${totalExpenses.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.error)
                    }
                    Column {
                        Text("Net Profit", style = MaterialTheme.typography.bodySmall)
                        Text("$currency${totalProfits.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4CAF50))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Net Profit Margin Margin: ${String.format("%.1f", marginPercentage)}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Visual Trend Line Graph
            Text(
                text = "Sales Trend (Gross)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            StylizedCard {
                InteractiveTrendGraph(
                    points = salesTrendPoints,
                    labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
                    currencySymbol = currency
                )
            }

            // Expense Distribution Chart
            Text(
                text = "Expense Distribution Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            StylizedCard {
                if (expenseCategories.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Add operational expenses to render slice breakdown.")
                    }
                } else {
                    DonutChart(slices = expenseCategories, currencySymbol = currency)
                }
            }

            // Export Reports Section
            Text(
                text = "Generate & Export Business Reports",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val result = viewModel.exportToCSV(context, "Sales")
                        Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Download, null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CSV Report")
                }

                Button(
                    onClick = {
                        val result = viewModel.exportToExcel(context, "Sales")
                        Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Icon(Icons.Default.TableChart, null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Excel Sheet")
                }
            }

            Button(
                onClick = {
                    val result = viewModel.backupDatabase(context)
                    Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(Icons.Default.Backup, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Full Database Backup (JSON)")
            }
        }
    }
}
