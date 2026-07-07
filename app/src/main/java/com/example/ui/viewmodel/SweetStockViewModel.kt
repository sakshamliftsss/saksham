package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SweetStockApplication
import com.example.data.api.GeminiClient
import com.example.data.model.*
import com.example.data.preferences.PreferencesHelper
import com.example.data.repository.SweetStockRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class SweetStockViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SweetStockRepository = (application as SweetStockApplication).repository
    private val preferencesHelper: PreferencesHelper = (application as SweetStockApplication).preferencesHelper

    // Core state flows
    val shopConfig = repository.shopConfig.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val inventory = repository.allInventoryItems.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val recipes = repository.allRecipes.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val productionLogs = repository.allProductionLogs.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val purchaseLogs = repository.allPurchaseLogs.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val saleLogs = repository.allSaleLogs.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val customers = repository.allCustomers.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val suppliers = repository.allSuppliers.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val expenses = repository.allExpenses.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val employees = repository.allEmployees.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val incomes = repository.allIncomes.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Preferences
    val isDarkMode = preferencesHelper.isDarkMode.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val selectedLanguage = preferencesHelper.selectedLanguage.stateIn(viewModelScope, SharingStarted.Eagerly, "English")
    val taxPercent = preferencesHelper.taxPercent.stateIn(viewModelScope, SharingStarted.Eagerly, 5.0)

    val bentoCardsOrder = preferencesHelper.bentoCardsOrder.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        PreferencesHelper.DEFAULT_BENTO_ORDER.split(",")
    )
    val bentoCardsVisibility = preferencesHelper.bentoCardsVisibility.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        PreferencesHelper.DEFAULT_BENTO_ORDER.split(",").toSet()
    )

    fun updateBentoCardsOrder(order: List<String>) {
        viewModelScope.launch {
            preferencesHelper.setBentoCardsOrder(order)
        }
    }

    fun updateBentoCardsVisibility(visibleCards: Set<String>) {
        viewModelScope.launch {
            preferencesHelper.setBentoCardsVisibility(visibleCards)
        }
    }

    // Security login state
    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    // AI States
    private val _aiInsight = MutableStateFlow<String>("")
    val aiInsight: StateFlow<String> = _aiInsight.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    init {
        // Automatically check if a shop profile exists and if setup is complete
        viewModelScope.launch {
            val config = repository.getShopConfigSync()
            if (config == null || !config.isSetupComplete) {
                // If no config, create a default offline placeholder profile
                repository.saveShopConfig(ShopConfig())
            }
        }
    }

    // Toggle Dark Mode
    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesHelper.setDarkMode(enabled)
        }
    }

    // Update Language
    fun updateLanguage(lang: String) {
        viewModelScope.launch {
            preferencesHelper.setLanguage(lang)
            shopConfig.value?.let {
                repository.saveShopConfig(it.copy(language = lang))
            }
        }
    }

    // Security Actions
    fun loginWithPin(pin: String): Boolean {
        val currentPin = shopConfig.value?.pinCode ?: "1234"
        val success = pin == currentPin
        if (success) {
            _isUserLoggedIn.value = true
        }
        return success
    }

    fun logout() {
        _isUserLoggedIn.value = false
    }

    // Setup Shop Profiles
    fun completeFirstTimeSetup(
        shopName: String,
        ownerName: String,
        mobile: String,
        altMobile: String,
        email: String,
        address: String,
        city: String,
        state: String,
        country: String,
        gst: String,
        pan: String,
        businessType: String,
        logoUri: String,
        currency: String,
        pin: String
    ) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val newConfig = ShopConfig(
                shopName = shopName,
                ownerName = ownerName,
                mobile = mobile,
                alternateMobile = altMobile,
                email = email,
                address = address,
                city = city,
                state = state,
                country = country,
                gstNumber = gst,
                panNumber = pan,
                businessType = businessType,
                logoUri = logoUri,
                openingDate = dateStr,
                currency = currency,
                pinCode = pin,
                isSetupComplete = true
            )
            repository.saveShopConfig(newConfig)
        }
    }

    // Update Settings Shop Info
    fun updateShopInfo(config: ShopConfig) {
        viewModelScope.launch {
            repository.saveShopConfig(config)
        }
    }

    // Inventory CRUD
    fun saveInventoryItem(item: InventoryItem) {
        viewModelScope.launch {
            repository.insertInventoryItem(item)
        }
    }

    fun deleteInventoryItem(item: InventoryItem) {
        viewModelScope.launch {
            repository.deleteInventoryItem(item)
        }
    }

    // Recipes CRUD
    fun saveRecipe(recipe: Recipe, ingredients: List<RecipeIngredient>) {
        viewModelScope.launch {
            repository.insertRecipe(recipe, ingredients)
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            repository.deleteRecipe(recipe)
        }
    }

    // Production Logging
    fun logProduction(
        recipeId: Int,
        batchNumber: String,
        quantityProduced: Double,
        gas: Double,
        labour: Double,
        packaging: Double,
        electricity: Double,
        water: Double,
        other: Double,
        waste: Double,
        remarks: String
    ) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            repository.recordProduction(
                recipeId = recipeId,
                batchNumber = batchNumber,
                quantityProduced = quantityProduced,
                productionDate = dateStr,
                actualGas = gas,
                actualLabour = labour,
                actualPackaging = packaging,
                actualElectricity = electricity,
                actualWater = water,
                actualOther = other,
                wasteQuantity = waste,
                remarks = remarks
            )
        }
    }

    // Purchase Logging
    fun logPurchase(
        supplierName: String,
        invoiceNumber: String,
        items: List<PurchaseItem>,
        discount: Double,
        transport: Double,
        loading: Double,
        other: Double,
        paymentMethod: String
    ) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            repository.recordPurchase(
                supplierName = supplierName,
                invoiceNumber = invoiceNumber,
                purchaseDate = dateStr,
                items = items,
                discount = discount,
                transport = transport,
                loading = loading,
                otherCharges = other,
                paymentMethod = paymentMethod
            )
        }
    }

    // Sales Logging
    fun logSale(
        invoiceNumber: String,
        customerName: String,
        customerPhone: String,
        items: List<SaleItem>,
        discount: Double,
        gst: Double,
        paymentMethod: String
    ) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            repository.recordSale(
                invoiceNumber = invoiceNumber,
                date = dateStr,
                customerName = customerName,
                customerPhone = customerPhone,
                items = items,
                discount = discount,
                gst = gst,
                paymentMethod = paymentMethod
            )
        }
    }

    // Expenses CRUD
    fun saveExpense(expense: Expense) {
        viewModelScope.launch {
            repository.insertExpense(expense)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    // Employees CRUD
    fun saveEmployee(employee: Employee) {
        viewModelScope.launch {
            repository.insertEmployee(employee)
        }
    }

    fun deleteEmployee(employee: Employee) {
        viewModelScope.launch {
            repository.deleteEmployee(employee)
        }
    }

    // Income CRUD
    fun saveIncome(income: Income) {
        viewModelScope.launch {
            repository.insertIncome(income)
        }
    }

    fun deleteIncome(income: Income) {
        viewModelScope.launch {
            repository.deleteIncome(income)
        }
    }

    // Customers CRUD
    fun saveCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.insertCustomer(customer)
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    // Suppliers CRUD
    fun saveSupplier(supplier: Supplier) {
        viewModelScope.launch {
            repository.insertSupplier(supplier)
        }
    }

    fun deleteSupplier(supplier: Supplier) {
        viewModelScope.launch {
            repository.deleteSupplier(supplier)
        }
    }

    // EXPORT FUNCTIONS (CSV/Excel/Backup/Restore)
    fun exportToCSV(context: Context, reportType: String): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "SweetStock_${reportType}_$dateFormat.csv"
        val data = when (reportType) {
            "Sales" -> {
                val header = "Invoice,Date,Customer,Phone,Grand Total,Payment Method,Profit\n"
                val rows = saleLogs.value.joinToString("\n") {
                    "${it.invoiceNumber},${it.date},${it.customerName},${it.customerPhone},${it.grandTotal},${it.paymentMethod},${it.netProfit}"
                }
                header + rows
            }
            "Inventory" -> {
                val header = "Item,Category,Current Stock,Unit,Cost,Expiry Date\n"
                val rows = inventory.value.joinToString("\n") {
                    "${it.name},${it.category},${it.quantity},${it.unit},${it.pricePerUnit},${it.expiryDate}"
                }
                header + rows
            }
            else -> {
                val header = "Expense Date,Category,Vendor,Amount,Payment Method,Notes\n"
                val rows = expenses.value.joinToString("\n") {
                    "${it.date},${it.category},${it.vendor},${it.amount},${it.paymentMethod},${it.notes}"
                }
                header + rows
            }
        }

        return try {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val file = File(dir, fileName)
            FileOutputStream(file).use { it.write(data.toByteArray()) }
            "Saved successfully to Documents/$fileName"
        } catch (e: Exception) {
            "Export failed: ${e.localizedMessage}"
        }
    }

    fun exportToExcel(context: Context, reportType: String): String {
        // Generates a tab-separated values file (.xls compatible) that loads directly in MS Excel
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "SweetStock_${reportType}_$dateFormat.xls"
        val data = when (reportType) {
            "Sales" -> {
                val header = "Invoice\tDate\tCustomer\tPhone\tGrand Total\tPayment Method\tProfit\n"
                val rows = saleLogs.value.joinToString("\n") {
                    "${it.invoiceNumber}\t${it.date}\t${it.customerName}\t${it.customerPhone}\t${it.grandTotal}\t${it.paymentMethod}\t${it.netProfit}"
                }
                header + rows
            }
            "Inventory" -> {
                val header = "Item\tCategory\tCurrent Stock\tUnit\tCost\tExpiry Date\n"
                val rows = inventory.value.joinToString("\n") {
                    "${it.name}\t${it.category}\t${it.quantity}\t${it.unit}\t${it.pricePerUnit}\t${it.expiryDate}"
                }
                header + rows
            }
            else -> {
                val header = "Expense Date\tCategory\tVendor\tAmount\tPayment Method\tNotes\n"
                val rows = expenses.value.joinToString("\n") {
                    "${it.date}\t${it.category}\t${it.vendor}\t${it.amount}\t${it.paymentMethod}\t${it.notes}"
                }
                header + rows
            }
        }

        return try {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val file = File(dir, fileName)
            FileOutputStream(file).use { it.write(data.toByteArray()) }
            "Saved Excel-compatible report to Documents/$fileName"
        } catch (e: Exception) {
            "Excel Export failed: ${e.localizedMessage}"
        }
    }

    // OFFLINE BACKUP SYSTEM
    data class DBBackupPayload(
        val shopConfig: ShopConfig?,
        val inventory: List<InventoryItem>,
        val recipes: List<Recipe>,
        val recipeIngredients: List<RecipeIngredient>,
        val productionLogs: List<ProductionLog>,
        val purchaseLogs: List<PurchaseLog>,
        val saleLogs: List<SaleLog>,
        val customers: List<Customer>,
        val suppliers: List<Supplier>,
        val expenses: List<Expense>,
        val employees: List<Employee>,
        val incomes: List<Income>
    )

    fun backupDatabase(context: Context): String {
        return try {
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(DBBackupPayload::class.java)

            // Gather ingredients synchronously by starting a coroutine that writes to a file
            var ingredientsList = emptyList<RecipeIngredient>()
            viewModelScope.launch {
                recipes.value.forEach {
                    ingredientsList = ingredientsList + repository.getIngredientsForRecipeSync(it.id)
                }
            }

            val payload = DBBackupPayload(
                shopConfig = shopConfig.value,
                inventory = inventory.value,
                recipes = recipes.value,
                recipeIngredients = ingredientsList,
                productionLogs = productionLogs.value,
                purchaseLogs = purchaseLogs.value,
                saleLogs = saleLogs.value,
                customers = customers.value,
                suppliers = suppliers.value,
                expenses = expenses.value,
                employees = employees.value,
                incomes = incomes.value
            )

            val json = adapter.toJson(payload)
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            val file = File(dir, "SweetStockPro_Backup.json")
            FileOutputStream(file).use { it.write(json.toByteArray()) }
            "Backup created! File saved: Downloads/SweetStockPro_Backup.json"
        } catch (e: Exception) {
            "Backup failed: ${e.localizedMessage}"
        }
    }

    fun restoreDatabase(context: Context, jsonUri: Uri): String {
        return try {
            val contentResolver = context.contentResolver
            val jsonString = contentResolver.openInputStream(jsonUri)?.bufferedReader()?.use { it.readText() }
            if (jsonString.isNullOrEmpty()) return "Error: Selected backup file is empty."

            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(DBBackupPayload::class.java)
            val payload = adapter.fromJson(jsonString) ?: return "Error: Invalid backup schema."

            viewModelScope.launch {
                payload.shopConfig?.let { repository.saveShopConfig(it) }
                payload.inventory.forEach { repository.insertInventoryItem(it) }
                payload.recipes.forEach { repository.insertRecipe(it, payload.recipeIngredients.filter { ing -> ing.recipeId == it.id }) }
                payload.productionLogs.forEach { repository.recordProduction(it.recipeId, it.batchNumber, it.quantityProduced, it.productionDate, it.gasCost, it.labourCost, it.packagingCost, it.electricityCost, it.waterCost, it.otherCost, it.wasteQuantity, it.remarks) }
                payload.purchaseLogs.forEach { repository.recordPurchase(it.supplierName, it.invoiceNumber, it.purchaseDate, emptyList(), it.discount, it.transport, it.loading, it.otherCharges, it.paymentMethod) }
                payload.saleLogs.forEach { repository.recordSale(it.invoiceNumber, it.date, it.customerName, it.customerPhone, emptyList(), it.discount, it.gst, it.paymentMethod) }
                payload.customers.forEach { repository.insertCustomer(it) }
                payload.suppliers.forEach { repository.insertSupplier(it) }
                payload.expenses.forEach { repository.insertExpense(it) }
                payload.employees.forEach { repository.insertEmployee(it) }
                payload.incomes.forEach { repository.insertIncome(it) }
            }
            "Restore complete! All records updated successfully."
        } catch (e: Exception) {
            "Restore failed: ${e.localizedMessage}"
        }
    }

    // AI INSIGHT GENERATION
    fun generateAiAnalysis() {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiInsight.value = "AI Business Assistant is computing forecasting metrics..."

            // Build shop state summary
            val currencySymbol = shopConfig.value?.currency ?: "₹"
            val totalInventoryVal = inventory.value.sumOf { it.quantity * it.pricePerUnit }
            val lowStockCount = inventory.value.filter { it.quantity <= it.minStock }.size
            val expensesTotal = expenses.value.sumOf { it.amount }
            val salesTotal = saleLogs.value.sumOf { it.grandTotal }
            val profitsTotal = saleLogs.value.sumOf { it.netProfit }

            val prompt = """
                As an expert Sweet Shop business consultant, analyze this shop data:
                - Shop Type: ${shopConfig.value?.businessType ?: "Sweet Shop"}
                - Total Sales: $currencySymbol$salesTotal
                - Total Expenses: $currencySymbol$expensesTotal
                - Accumulated Net Profits: $currencySymbol$profitsTotal
                - Inventory Value: $currencySymbol$totalInventoryVal
                - Low Stock Items: $lowStockCount

                Provide a highly actionable, structured, professional and bulleted analysis. Include:
                1. Low stock prediction & inventory demand forecasting.
                2. Smart reorder suggestions.
                3. High & Low profitability sweet insights.
                4. A suggested selling markup/pricing formula based on ingredient costs.
                5. Basic expense anomaly checks.
                6. A personalized recommendations checklist to increase profit margin by 5-10%.
                Make the tone business-focused, professional, elegant, and fully practical. Keep it concise.
            """.trimIndent()

            val insight = GeminiClient.generateBusinessInsight(prompt)
            _aiInsight.value = insight
            _isAiLoading.value = false
        }
    }
}
