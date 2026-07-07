package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@Entity(tableName = "shop_config")
data class ShopConfig(
    @PrimaryKey val id: Int = 1,
    val shopName: String = "",
    val ownerName: String = "",
    val mobile: String = "",
    val alternateMobile: String = "",
    val email: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val country: String = "",
    val gstNumber: String = "",
    val panNumber: String = "",
    val businessType: String = "Sweet Shop",
    val logoUri: String = "",
    val openingDate: String = "",
    val currency: String = "₹",
    val language: String = "English",
    val pinCode: String = "1234",
    val isSetupComplete: Boolean = false,
    val useBiometrics: Boolean = false
)

@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val supplier: String = "",
    val purchaseDate: String = "",
    val invoiceNumber: String = "",
    val batchNumber: String = "",
    val quantity: Double = 0.0,
    val unit: String = "Kg", // Kg, Gram, Liter, Piece, Packet, Box
    val pricePerUnit: Double = 0.0,
    val gstPercent: Double = 0.0,
    val transportCost: Double = 0.0,
    val totalCost: Double = 0.0,
    val expiryDate: String = "",
    val minStock: Double = 0.0,
    val barcode: String = "",
    val storageLocation: String = "",
    val notes: String = "",
    val isRawMaterial: Boolean = true, // true for raw materials, false for finished sweets
    val sellingPrice: Double = 0.0 // for finished sweets
)

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String = "",
    val gasCost: Double = 0.0,
    val labourCost: Double = 0.0,
    val packagingCost: Double = 0.0,
    val electricityCost: Double = 0.0,
    val waterCost: Double = 0.0,
    val otherCost: Double = 0.0,
    val expectedYield: Double = 1.0,
    val yieldUnit: String = "Kg"
)

@Entity(tableName = "recipe_ingredients")
data class RecipeIngredient(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recipeId: Int,
    val inventoryItemId: Int, // Raw material item ID
    val quantityRequired: Double
)

@Entity(tableName = "production_logs")
data class ProductionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recipeId: Int,
    val recipeName: String,
    val batchNumber: String = "",
    val quantityProduced: Double = 0.0,
    val productionDate: String = "",
    val gasCost: Double = 0.0,
    val labourCost: Double = 0.0,
    val electricityCost: Double = 0.0,
    val packagingCost: Double = 0.0,
    val waterCost: Double = 0.0,
    val otherCost: Double = 0.0,
    val wasteQuantity: Double = 0.0,
    val remarks: String = "",
    val totalCost: Double = 0.0,
    val costPerUnit: Double = 0.0,
    val efficiency: Double = 100.0 // produced vs expected yield %
)

@Entity(tableName = "purchase_logs")
data class PurchaseLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val supplierName: String = "",
    val invoiceNumber: String = "",
    val purchaseDate: String = "",
    val itemsJson: String = "[]", // List<PurchaseItem>
    val discount: Double = 0.0,
    val transport: Double = 0.0,
    val loading: Double = 0.0,
    val otherCharges: Double = 0.0,
    val grandTotal: Double = 0.0,
    val paymentMethod: String = "Cash" // Cash, UPI, Bank, Credit
)

@Entity(tableName = "sale_logs")
data class SaleLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val invoiceNumber: String = "",
    val date: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val itemsJson: String = "[]", // List<SaleItem>
    val discount: Double = 0.0,
    val gst: Double = 0.0,
    val grandTotal: Double = 0.0,
    val paymentMethod: String = "Cash", // Cash, UPI, Card, Bank, Credit
    val totalCost: Double = 0.0, // COGS (Cost of Goods Sold)
    val netProfit: Double = 0.0
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val gstNumber: String = "",
    val creditLimit: Double = 0.0,
    val pendingBalance: Double = 0.0
)

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val gst: String = "",
    val itemsSupplied: String = "",
    val outstandingPayment: Double = 0.0
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val category: String, // default: Milk, Sugar, Packaging, Rent, Salary, etc.
    val vendor: String = "",
    val amount: Double = 0.0,
    val gst: Double = 0.0,
    val description: String = "",
    val paymentMethod: String = "Cash",
    val notes: String = "",
    val receiptUri: String = ""
)

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val role: String = "Staff",
    val salary: Double = 0.0,
    val joiningDate: String = "",
    val attendanceJson: String = "{}", // Map<String, String> date -> status
    val advanceSalary: Double = 0.0,
    val leavesCount: Int = 0
)

@Entity(tableName = "incomes")
data class Income(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val source: String,
    val amount: Double = 0.0,
    val date: String = "",
    val description: String = ""
)

data class PurchaseItem(
    val id: Int,
    val name: String,
    val quantity: Double,
    val unit: String,
    val pricePerUnit: Double,
    val gstPercent: Double,
    val totalCost: Double
)

data class SaleItem(
    val id: Int, // Finished Sweet item ID
    val name: String,
    val quantity: Double,
    val rate: Double,
    val discountPercent: Double,
    val gstPercent: Double,
    val totalCost: Double, // standard production cost of 1 unit * quantity
    val totalSale: Double  // standard sale price * quantity (before manual discounts)
)

class Converters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @TypeConverter
    fun fromPurchaseItemList(value: List<PurchaseItem>?): String {
        if (value == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, PurchaseItem::class.java)
        val adapter = moshi.adapter<List<PurchaseItem>>(type)
        return adapter.toJson(value)
    }

    @TypeConverter
    fun toPurchaseItemList(value: String?): List<PurchaseItem> {
        if (value.isNullOrEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, PurchaseItem::class.java)
        val adapter = moshi.adapter<List<PurchaseItem>>(type)
        return adapter.fromJson(value) ?: emptyList()
    }

    @TypeConverter
    fun fromSaleItemList(value: List<SaleItem>?): String {
        if (value == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, SaleItem::class.java)
        val adapter = moshi.adapter<List<SaleItem>>(type)
        return adapter.toJson(value)
    }

    @TypeConverter
    fun toSaleItemList(value: String?): List<SaleItem> {
        if (value.isNullOrEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, SaleItem::class.java)
        val adapter = moshi.adapter<List<SaleItem>>(type)
        return adapter.fromJson(value) ?: emptyList()
    }

    @TypeConverter
    fun fromMap(value: Map<String, String>?): String {
        if (value == null) return "{}"
        val type = Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
        val adapter = moshi.adapter<Map<String, String>>(type)
        return adapter.toJson(value)
    }

    @TypeConverter
    fun toMap(value: String?): Map<String, String> {
        if (value.isNullOrEmpty()) return emptyMap()
        val type = Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
        val adapter = moshi.adapter<Map<String, String>>(type)
        return adapter.fromJson(value) ?: emptyMap()
    }
}
