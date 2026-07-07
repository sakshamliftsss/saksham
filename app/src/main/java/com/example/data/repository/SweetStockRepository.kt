package com.example.data.repository

import android.content.Context
import com.example.data.database.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.*

class SweetStockRepository(private val db: AppDatabase) {

    // DAOs
    private val shopConfigDao = db.shopConfigDao()
    private val inventoryItemDao = db.inventoryItemDao()
    private val recipeDao = db.recipeDao()
    private val recipeIngredientDao = db.recipeIngredientDao()
    private val productionLogDao = db.productionLogDao()
    private val purchaseLogDao = db.purchaseLogDao()
    private val saleLogDao = db.saleLogDao()
    private val customerDao = db.customerDao()
    private val supplierDao = db.supplierDao()
    private val expenseDao = db.expenseDao()
    private val employeeDao = db.employeeDao()
    private val incomeDao = db.incomeDao()

    // Flows
    val shopConfig: Flow<ShopConfig?> = shopConfigDao.getShopConfig()
    val allInventoryItems: Flow<List<InventoryItem>> = inventoryItemDao.getAllItems()
    val rawMaterials: Flow<List<InventoryItem>> = inventoryItemDao.getRawMaterials()
    val finishedSweets: Flow<List<InventoryItem>> = inventoryItemDao.getFinishedSweets()
    val allRecipes: Flow<List<Recipe>> = recipeDao.getAllRecipes()
    val allProductionLogs: Flow<List<ProductionLog>> = productionLogDao.getAllLogs()
    val allPurchaseLogs: Flow<List<PurchaseLog>> = purchaseLogDao.getAllLogs()
    val allSaleLogs: Flow<List<SaleLog>> = saleLogDao.getAllLogs()
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
    val allSuppliers: Flow<List<Supplier>> = supplierDao.getAllSuppliers()
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()
    val allEmployees: Flow<List<Employee>> = employeeDao.getAllEmployees()
    val allIncomes: Flow<List<Income>> = incomeDao.getAllIncomes()

    // Shop Config
    suspend fun getShopConfigSync(): ShopConfig? = shopConfigDao.getShopConfigSync()
    suspend fun saveShopConfig(config: ShopConfig) = shopConfigDao.insertOrUpdate(config)

    // Inventory
    suspend fun insertInventoryItem(item: InventoryItem): Long = inventoryItemDao.insert(item)
    suspend fun updateInventoryItem(item: InventoryItem) = inventoryItemDao.update(item)
    suspend fun deleteInventoryItem(item: InventoryItem) = inventoryItemDao.delete(item)
    suspend fun deleteInventoryItemById(id: Int) = inventoryItemDao.deleteById(id)
    fun getInventoryItemById(id: Int): Flow<InventoryItem?> = inventoryItemDao.getItemById(id)

    // Recipes
    suspend fun insertRecipe(recipe: Recipe, ingredients: List<RecipeIngredient>): Long {
        val recipeId = recipeDao.insert(recipe)
        recipeIngredientDao.deleteByRecipeId(recipeId.toInt())
        ingredients.forEach {
            recipeIngredientDao.insert(it.copy(recipeId = recipeId.toInt()))
        }
        return recipeId
    }

    suspend fun updateRecipe(recipe: Recipe, ingredients: List<RecipeIngredient>) {
        recipeDao.update(recipe)
        recipeIngredientDao.deleteByRecipeId(recipe.id)
        ingredients.forEach {
            recipeIngredientDao.insert(it.copy(recipeId = recipe.id))
        }
    }

    suspend fun deleteRecipe(recipe: Recipe) {
        recipeDao.delete(recipe)
        recipeIngredientDao.deleteByRecipeId(recipe.id)
    }

    fun getIngredientsForRecipe(recipeId: Int): Flow<List<RecipeIngredient>> =
        recipeIngredientDao.getIngredientsForRecipe(recipeId)

    suspend fun getIngredientsForRecipeSync(recipeId: Int): List<RecipeIngredient> =
        recipeIngredientDao.getIngredientsForRecipeSync(recipeId)

    // Business Action: Purchases
    suspend fun recordPurchase(
        supplierName: String,
        invoiceNumber: String,
        purchaseDate: String,
        items: List<PurchaseItem>,
        discount: Double,
        transport: Double,
        loading: Double,
        otherCharges: Double,
        paymentMethod: String
    ): Long {
        // Calculate grand total
        val itemsSubtotal = items.sumOf { it.totalCost }
        val tax = items.sumOf { it.totalCost * (it.gstPercent / 100.0) }
        val grandTotal = itemsSubtotal + tax + transport + loading + otherCharges - discount

        // 1. Save Purchase Log
        val converters = Converters()
        val itemsJson = converters.fromPurchaseItemList(items)
        val purchaseLog = PurchaseLog(
            supplierName = supplierName,
            invoiceNumber = invoiceNumber,
            purchaseDate = purchaseDate,
            itemsJson = itemsJson,
            discount = discount,
            transport = transport,
            loading = loading,
            otherCharges = otherCharges,
            grandTotal = grandTotal,
            paymentMethod = paymentMethod
        )
        val purchaseId = purchaseLogDao.insert(purchaseLog)

        // 2. Update Inventory
        items.forEach { purchaseItem ->
            val existing = inventoryItemDao.getItemByIdSync(purchaseItem.id)
            if (existing != null) {
                val currentQty = existing.quantity
                val addedQty = purchaseItem.quantity
                val totalQty = currentQty + addedQty

                // Calculate average cost per unit
                val currentCostBasis = currentQty * existing.pricePerUnit
                val addedCostBasis = addedQty * purchaseItem.pricePerUnit
                val newAvgCost = if (totalQty > 0) (currentCostBasis + addedCostBasis) / totalQty else purchaseItem.pricePerUnit

                val updatedItem = existing.copy(
                    quantity = totalQty,
                    pricePerUnit = newAvgCost,
                    totalCost = totalQty * newAvgCost,
                    purchaseDate = purchaseDate,
                    invoiceNumber = invoiceNumber
                )
                inventoryItemDao.update(updatedItem)
            } else {
                // If it is a new item not found in DB, insert it
                val newItem = InventoryItem(
                    name = purchaseItem.name,
                    category = "Raw Material",
                    supplier = supplierName,
                    purchaseDate = purchaseDate,
                    invoiceNumber = invoiceNumber,
                    quantity = purchaseItem.quantity,
                    unit = purchaseItem.unit,
                    pricePerUnit = purchaseItem.pricePerUnit,
                    gstPercent = purchaseItem.gstPercent,
                    totalCost = purchaseItem.totalCost,
                    isRawMaterial = true
                )
                inventoryItemDao.insert(newItem)
            }
        }

        // 3. Update Supplier Outstanding if payment method is "Credit"
        if (paymentMethod.equals("Credit", ignoreCase = true) && supplierName.isNotEmpty()) {
            db.supplierDao().getAllSuppliers().collect { suppliers ->
                val supplier = suppliers.find { it.name.equals(supplierName, ignoreCase = true) }
                if (supplier != null) {
                    db.supplierDao().update(
                        supplier.copy(
                            outstandingPayment = supplier.outstandingPayment + grandTotal
                        )
                    )
                } else {
                    db.supplierDao().insert(
                        Supplier(
                            name = supplierName,
                            outstandingPayment = grandTotal
                        )
                    )
                }
            }
        }

        return purchaseId
    }

    // Business Action: Production
    suspend fun recordProduction(
        recipeId: Int,
        batchNumber: String,
        quantityProduced: Double,
        productionDate: String,
        actualGas: Double,
        actualLabour: Double,
        actualPackaging: Double,
        actualElectricity: Double,
        actualWater: Double,
        actualOther: Double,
        wasteQuantity: Double,
        remarks: String
    ): Long {
        val recipe = recipeDao.getRecipeByIdSync(recipeId) ?: return -1
        val ingredients = recipeIngredientDao.getIngredientsForRecipeSync(recipeId)

        // 1. Calculate actual raw material costs from inventory average cost
        var totalRawMaterialCost = 0.0
        ingredients.forEach { ingredient ->
            val rawItem = inventoryItemDao.getItemByIdSync(ingredient.inventoryItemId)
            if (rawItem != null) {
                // Deduct inventory
                val requiredQty = ingredient.quantityRequired * quantityProduced
                val remainingQty = (rawItem.quantity - requiredQty).coerceAtLeast(0.0)
                inventoryItemDao.update(
                    rawItem.copy(
                        quantity = remainingQty,
                        totalCost = remainingQty * rawItem.pricePerUnit
                    )
                )

                totalRawMaterialCost += (ingredient.quantityRequired * rawItem.pricePerUnit) * quantityProduced
            }
        }

        // 2. Compute overall costs
        val operationalCost = actualGas + actualLabour + actualPackaging + actualElectricity + actualWater + actualOther
        val totalCost = totalRawMaterialCost + operationalCost
        val costPerUnit = if (quantityProduced > 0) totalCost / quantityProduced else 0.0

        // Calculate production efficiency (actual vs expected recipe yield)
        val expected = recipe.expectedYield
        val efficiency = if (expected > 0) (quantityProduced / expected) * 100.0 else 100.0

        // 3. Save Production Log
        val log = ProductionLog(
            recipeId = recipeId,
            recipeName = recipe.name,
            batchNumber = batchNumber,
            quantityProduced = quantityProduced,
            productionDate = productionDate,
            gasCost = actualGas,
            labourCost = actualLabour,
            electricityCost = actualElectricity,
            packagingCost = actualPackaging,
            waterCost = actualWater,
            otherCost = actualOther,
            wasteQuantity = wasteQuantity,
            remarks = remarks,
            totalCost = totalCost,
            costPerUnit = costPerUnit,
            efficiency = efficiency
        )
        val logId = productionLogDao.insert(log)

        // 4. Update or Insert Finished Sweet Inventory
        db.inventoryItemDao().getAllItems().collect { items ->
            val finishedItem = items.find { it.name.equals(recipe.name, ignoreCase = true) && !it.isRawMaterial }
            if (finishedItem != null) {
                // Average cost calculation for finished goods
                val currentQty = finishedItem.quantity
                val currentCostBasis = currentQty * finishedItem.pricePerUnit
                val newQty = currentQty + quantityProduced
                val newAvgCost = if (newQty > 0) (currentCostBasis + totalCost) / newQty else costPerUnit

                inventoryItemDao.update(
                    finishedItem.copy(
                        quantity = newQty,
                        pricePerUnit = newAvgCost,
                        totalCost = newQty * newAvgCost,
                        batchNumber = batchNumber,
                        purchaseDate = productionDate
                    )
                )
            } else {
                // Create new finished sweet
                val newItem = InventoryItem(
                    name = recipe.name,
                    category = "Sweets",
                    quantity = quantityProduced,
                    unit = recipe.yieldUnit,
                    pricePerUnit = costPerUnit,
                    totalCost = totalCost,
                    isRawMaterial = false,
                    sellingPrice = costPerUnit * 1.4 // Default markup of 40%
                )
                inventoryItemDao.insert(newItem)
            }
        }

        return logId
    }

    // Business Action: Sales
    suspend fun recordSale(
        invoiceNumber: String,
        date: String,
        customerName: String,
        customerPhone: String,
        items: List<SaleItem>,
        discount: Double,
        gst: Double,
        paymentMethod: String
    ): Long {
        // Calculate aggregates
        val itemsSubtotal = items.sumOf { it.totalSale }
        val grandTotal = (itemsSubtotal + gst - discount).coerceAtLeast(0.0)
        val totalCostOfGoods = items.sumOf { it.totalCost } // standard production cost of the inventory sold
        val netProfit = grandTotal - totalCostOfGoods

        // 1. Save Sale Log
        val converters = Converters()
        val itemsJson = converters.fromSaleItemList(items)
        val saleLog = SaleLog(
            invoiceNumber = invoiceNumber,
            date = date,
            customerName = customerName,
            customerPhone = customerPhone,
            itemsJson = itemsJson,
            discount = discount,
            gst = gst,
            grandTotal = grandTotal,
            paymentMethod = paymentMethod,
            totalCost = totalCostOfGoods,
            netProfit = netProfit
        )
        val saleId = saleLogDao.insert(saleLog)

        // 2. Reduce Finished Goods Stock
        items.forEach { saleItem ->
            val existing = inventoryItemDao.getItemByIdSync(saleItem.id)
            if (existing != null) {
                val newQty = (existing.quantity - saleItem.quantity).coerceAtLeast(0.0)
                inventoryItemDao.update(
                    existing.copy(
                        quantity = newQty,
                        totalCost = newQty * existing.pricePerUnit
                    )
                )
            }
        }

        // 3. Update Customer Pending Balance if "Credit" sales
        if (paymentMethod.equals("Credit", ignoreCase = true) && customerName.isNotEmpty()) {
            db.customerDao().getAllCustomers().collect { customers ->
                val customer = customers.find { it.name.equals(customerName, ignoreCase = true) }
                if (customer != null) {
                    db.customerDao().update(
                        customer.copy(
                            pendingBalance = customer.pendingBalance + grandTotal
                        )
                    )
                } else {
                    db.customerDao().insert(
                        Customer(
                            name = customerName,
                            phone = customerPhone,
                            pendingBalance = grandTotal
                        )
                    )
                }
            }
        }

        return saleId
    }

    // Customers
    suspend fun insertCustomer(customer: Customer) = customerDao.insert(customer)
    suspend fun updateCustomer(customer: Customer) = customerDao.update(customer)
    suspend fun deleteCustomer(customer: Customer) = customerDao.delete(customer)

    // Suppliers
    suspend fun insertSupplier(supplier: Supplier) = supplierDao.insert(supplier)
    suspend fun updateSupplier(supplier: Supplier) = supplierDao.update(supplier)
    suspend fun deleteSupplier(supplier: Supplier) = supplierDao.delete(supplier)

    // Expenses
    suspend fun insertExpense(expense: Expense): Long = expenseDao.insert(expense)
    suspend fun updateExpense(expense: Expense) = expenseDao.update(expense)
    suspend fun deleteExpense(expense: Expense) = expenseDao.delete(expense)
    suspend fun deleteExpenseById(id: Int) = expenseDao.deleteById(id)

    // Employees
    suspend fun insertEmployee(employee: Employee) = employeeDao.insert(employee)
    suspend fun updateEmployee(employee: Employee) = employeeDao.update(employee)
    suspend fun deleteEmployee(employee: Employee) = employeeDao.delete(employee)

    // Incomes
    suspend fun insertIncome(income: Income) = incomeDao.insert(income)
    suspend fun updateIncome(income: Income) = incomeDao.update(income)
    suspend fun deleteIncome(income: Income) = incomeDao.delete(income)
}
