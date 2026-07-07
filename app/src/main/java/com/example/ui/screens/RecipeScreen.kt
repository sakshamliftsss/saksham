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
import com.example.data.model.Recipe
import com.example.data.model.RecipeIngredient
import com.example.ui.components.StylizedCard
import com.example.ui.components.StylizedTextField
import com.example.ui.viewmodel.SweetStockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
    viewModel: SweetStockViewModel,
    onBack: () -> Unit
) {
    val recipes by viewModel.recipes.collectAsState()
    val inventory by viewModel.inventory.collectAsState()
    val rawMaterials = inventory.filter { it.isRawMaterial }
    val shopConfig by viewModel.shopConfig.collectAsState()
    val currency = shopConfig?.currency ?: "₹"

    var showCreateDialog by remember { mutableStateOf(false) }
    var showProductionDialog by remember { mutableStateOf<Recipe?>(null) }

    // Recipe Form
    var recipeName by remember { mutableStateOf("") }
    var recipeDesc by remember { mutableStateOf("") }
    var expectedYield by remember { mutableStateOf("10.0") }
    var yieldUnit by remember { mutableStateOf("Kg") }
    var gasCost by remember { mutableStateOf("50") }
    var labourCost by remember { mutableStateOf("150") }
    var packagingCost by remember { mutableStateOf("25") }
    var electricityCost by remember { mutableStateOf("30") }
    var waterCost by remember { mutableStateOf("10") }
    var otherCost by remember { mutableStateOf("5") }

    // Dynamic ingredients input list for the recipe
    val recipeIngredients = remember { mutableStateListOf<Pair<InventoryItem, Double>>() }
    var selectedRawMaterial by remember { mutableStateOf<InventoryItem?>(null) }
    var ingredientQty by remember { mutableStateOf("") }

    // Production log form states
    var batchNumber by remember { mutableStateOf("BCH-001") }
    var produceQuantity by remember { mutableStateOf("") }
    var actualGas by remember { mutableStateOf("") }
    var actualLabour by remember { mutableStateOf("") }
    var actualPackaging by remember { mutableStateOf("") }
    var actualElectricity by remember { mutableStateOf("") }
    var actualWater by remember { mutableStateOf("") }
    var actualOther by remember { mutableStateOf("") }
    var wasteQty by remember { mutableStateOf("0.0") }
    var remarks by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recipes & Production", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        recipeName = ""
                        recipeDesc = ""
                        expectedYield = "10.0"
                        yieldUnit = "Kg"
                        gasCost = "50"
                        labourCost = "150"
                        packagingCost = "25"
                        electricityCost = "30"
                        waterCost = "10"
                        otherCost = "5"
                        recipeIngredients.clear()
                        showCreateDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Create Recipe")
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
                text = "Select a Recipe to Edit or Log Production",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            if (recipes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No recipes created. Tap + to build your first recipe.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recipes) { recipe ->
                        StylizedCard {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1.5f)) {
                                        Text(
                                            text = recipe.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Yield: ${recipe.expectedYield} ${recipe.yieldUnit}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            batchNumber = "BCH-" + (100..999).random().toString()
                                            produceQuantity = recipe.expectedYield.toString()
                                            actualGas = recipe.gasCost.toString()
                                            actualLabour = recipe.labourCost.toString()
                                            actualPackaging = recipe.packagingCost.toString()
                                            actualElectricity = recipe.electricityCost.toString()
                                            actualWater = recipe.waterCost.toString()
                                            actualOther = recipe.otherCost.toString()
                                            wasteQty = "0.0"
                                            remarks = ""
                                            showProductionDialog = recipe
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(Icons.Default.PrecisionManufacturing, null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Produce")
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = recipe.description.ifEmpty { "No recipe details added." },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Base Overheads: $currency${(recipe.gasCost + recipe.labourCost + recipe.packagingCost + recipe.electricityCost + recipe.waterCost + recipe.otherCost)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Avg Cost: $currency${String.format("%.2f", (recipe.gasCost + recipe.labourCost + recipe.packagingCost + recipe.electricityCost + recipe.waterCost + recipe.otherCost) / recipe.expectedYield)}/${recipe.yieldUnit}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal to create recipe
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Build New Sweet Recipe") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StylizedTextField(value = recipeName, onValueChange = { recipeName = it }, label = "Recipe Name (e.g. Rasgulla) *")
                    StylizedTextField(value = recipeDesc, onValueChange = { recipeDesc = it }, label = "Short Instruction/Description")

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StylizedTextField(value = expectedYield, onValueChange = { expectedYield = it }, label = "Expected Yield *", modifier = Modifier.weight(1.5f))
                        StylizedTextField(value = yieldUnit, onValueChange = { yieldUnit = it }, label = "Unit *", modifier = Modifier.weight(1f))
                    }

                    Text("Standard Overhead Costs", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StylizedTextField(value = gasCost, onValueChange = { gasCost = it }, label = "Gas cost", modifier = Modifier.weight(1f))
                        StylizedTextField(value = labourCost, onValueChange = { labourCost = it }, label = "Labour Cost", modifier = Modifier.weight(1f))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StylizedTextField(value = packagingCost, onValueChange = { packagingCost = it }, label = "Packaging", modifier = Modifier.weight(1f))
                        StylizedTextField(value = electricityCost, onValueChange = { electricityCost = it }, label = "Electricity", modifier = Modifier.weight(1f))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StylizedTextField(value = waterCost, onValueChange = { waterCost = it }, label = "Water", modifier = Modifier.weight(1f))
                        StylizedTextField(value = otherCost, onValueChange = { otherCost = it }, label = "Other", modifier = Modifier.weight(1f))
                    }

                    // Ingredients management list
                    Text("Add Ingredients Required", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    // Drop down of ingredients in inventory
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StylizedTextField(
                                value = ingredientQty,
                                onValueChange = { ingredientQty = it },
                                label = "Qty required (per yield unit)",
                                modifier = Modifier.weight(1.5f)
                            )
                            Button(
                                onClick = {
                                    val mat = selectedRawMaterial
                                    val qty = ingredientQty.toDoubleOrNull() ?: 0.0
                                    if (mat != null && qty > 0.0) {
                                        recipeIngredients.add(Pair(mat, qty))
                                        selectedRawMaterial = null
                                        ingredientQty = ""
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Add")
                            }
                        }
                    } else {
                        Text("Please create raw material inventory items first.", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }

                    // Display added ingredients
                    recipeIngredients.forEachIndexed { idx, pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${pair.first.name}: ${pair.second} ${pair.first.unit}")
                            IconButton(onClick = { recipeIngredients.removeAt(idx) }) {
                                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val yieldVal = expectedYield.toDoubleOrNull() ?: 1.0
                        if (recipeName.isNotEmpty() && expectedYield.isNotEmpty()) {
                            val newRecipe = Recipe(
                                name = recipeName,
                                description = recipeDesc,
                                expectedYield = yieldVal,
                                yieldUnit = yieldUnit,
                                gasCost = gasCost.toDoubleOrNull() ?: 0.0,
                                labourCost = labourCost.toDoubleOrNull() ?: 0.0,
                                packagingCost = packagingCost.toDoubleOrNull() ?: 0.0,
                                electricityCost = electricityCost.toDoubleOrNull() ?: 0.0,
                                waterCost = waterCost.toDoubleOrNull() ?: 0.0,
                                otherCost = otherCost.toDoubleOrNull() ?: 0.0
                            )
                            val ingredientsList = recipeIngredients.map {
                                RecipeIngredient(
                                    recipeId = 0,
                                    inventoryItemId = it.first.id,
                                    quantityRequired = it.second
                                )
                            }
                            viewModel.saveRecipe(newRecipe, ingredientsList)
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("Save Recipe")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal to run a production batch
    showProductionDialog?.let { recipe ->
        AlertDialog(
            onDismissRequest = { showProductionDialog = null },
            title = { Text("Log Production: ${recipe.name}") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StylizedTextField(value = batchNumber, onValueChange = { batchNumber = it }, label = "Batch Number")
                    StylizedTextField(value = produceQuantity, onValueChange = { produceQuantity = it }, label = "Quantity to Produce (${recipe.yieldUnit}) *")

                    Text("Overhead Costs during Batch", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StylizedTextField(value = actualGas, onValueChange = { actualGas = it }, label = "Gas cost", modifier = Modifier.weight(1f))
                        StylizedTextField(value = actualLabour, onValueChange = { actualLabour = it }, label = "Labour Cost", modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StylizedTextField(value = actualPackaging, onValueChange = { actualPackaging = it }, label = "Packaging", modifier = Modifier.weight(1f))
                        StylizedTextField(value = actualElectricity, onValueChange = { actualElectricity = it }, label = "Electricity", modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StylizedTextField(value = actualWater, onValueChange = { actualWater = it }, label = "Water", modifier = Modifier.weight(1f))
                        StylizedTextField(value = actualOther, onValueChange = { actualOther = it }, label = "Other", modifier = Modifier.weight(1f))
                    }

                    StylizedTextField(value = wasteQty, onValueChange = { wasteQty = it }, label = "Waste Quantity (${recipe.yieldUnit})")
                    StylizedTextField(value = remarks, onValueChange = { remarks = it }, label = "Remarks/Log Notes")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = produceQuantity.toDoubleOrNull() ?: 0.0
                        if (qty > 0.0) {
                            viewModel.logProduction(
                                recipeId = recipe.id,
                                batchNumber = batchNumber,
                                quantityProduced = qty,
                                gas = actualGas.toDoubleOrNull() ?: 0.0,
                                labour = actualLabour.toDoubleOrNull() ?: 0.0,
                                packaging = actualPackaging.toDoubleOrNull() ?: 0.0,
                                electricity = actualElectricity.toDoubleOrNull() ?: 0.0,
                                water = actualWater.toDoubleOrNull() ?: 0.0,
                                other = actualOther.toDoubleOrNull() ?: 0.0,
                                waste = wasteQty.toDoubleOrNull() ?: 0.0,
                                remarks = remarks
                            )
                            showProductionDialog = null
                        }
                    }
                ) {
                    Text("Confirm Production")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProductionDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
