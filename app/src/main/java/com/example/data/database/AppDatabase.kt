package com.example.data.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopConfigDao {
    @Query("SELECT * FROM shop_config WHERE id = 1 LIMIT 1")
    fun getShopConfig(): Flow<ShopConfig?>

    @Query("SELECT * FROM shop_config WHERE id = 1 LIMIT 1")
    suspend fun getShopConfigSync(): ShopConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(config: ShopConfig)
}

@Dao
interface InventoryItemDao {
    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    fun getAllItems(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE isRawMaterial = 1 ORDER BY name ASC")
    fun getRawMaterials(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE isRawMaterial = 0 ORDER BY name ASC")
    fun getFinishedSweets(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE id = :id LIMIT 1")
    suspend fun getItemByIdSync(id: Int): InventoryItem?

    @Query("SELECT * FROM inventory_items WHERE id = :id LIMIT 1")
    fun getItemById(id: Int): Flow<InventoryItem?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: InventoryItem): Long

    @Update
    suspend fun update(item: InventoryItem)

    @Delete
    suspend fun delete(item: InventoryItem)

    @Query("DELETE FROM inventory_items WHERE id = :id")
    suspend fun deleteById(id: Int)
}

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY name ASC")
    fun getAllRecipes(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE id = :id LIMIT 1")
    suspend fun getRecipeByIdSync(id: Int): Recipe?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: Recipe): Long

    @Update
    suspend fun update(recipe: Recipe)

    @Delete
    suspend fun delete(recipe: Recipe)
}

@Dao
interface RecipeIngredientDao {
    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId")
    fun getIngredientsForRecipe(recipeId: Int): Flow<List<RecipeIngredient>>

    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun getIngredientsForRecipeSync(recipeId: Int): List<RecipeIngredient>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ingredient: RecipeIngredient)

    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun deleteByRecipeId(recipeId: Int)

    @Query("DELETE FROM recipe_ingredients WHERE id = :id")
    suspend fun deleteById(id: Int)
}

@Dao
interface ProductionLogDao {
    @Query("SELECT * FROM production_logs ORDER BY productionDate DESC, id DESC")
    fun getAllLogs(): Flow<List<ProductionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: ProductionLog): Long

    @Query("DELETE FROM production_logs WHERE id = :id")
    suspend fun deleteById(id: Int)
}

@Dao
interface PurchaseLogDao {
    @Query("SELECT * FROM purchase_logs ORDER BY purchaseDate DESC, id DESC")
    fun getAllLogs(): Flow<List<PurchaseLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: PurchaseLog): Long

    @Query("DELETE FROM purchase_logs WHERE id = :id")
    suspend fun deleteById(id: Int)
}

@Dao
interface SaleLogDao {
    @Query("SELECT * FROM sale_logs ORDER BY date DESC, id DESC")
    fun getAllLogs(): Flow<List<SaleLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: SaleLog): Long

    @Query("DELETE FROM sale_logs WHERE id = :id")
    suspend fun deleteById(id: Int)
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: Customer): Long

    @Update
    suspend fun update(customer: Customer)

    @Delete
    suspend fun delete(customer: Customer)

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getByIdSync(id: Int): Customer?
}

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<Supplier>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(supplier: Supplier): Long

    @Update
    suspend fun update(supplier: Supplier)

    @Delete
    suspend fun delete(supplier: Supplier)

    @Query("SELECT * FROM suppliers WHERE id = :id LIMIT 1")
    suspend fun getByIdSync(id: Int): Supplier?
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC, id DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteById(id: Int)
}

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAllEmployees(): Flow<List<Employee>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(employee: Employee): Long

    @Update
    suspend fun update(employee: Employee)

    @Delete
    suspend fun delete(employee: Employee)

    @Query("SELECT * FROM employees WHERE id = :id LIMIT 1")
    suspend fun getByIdSync(id: Int): Employee?
}

@Dao
interface IncomeDao {
    @Query("SELECT * FROM incomes ORDER BY date DESC, id DESC")
    fun getAllIncomes(): Flow<List<Income>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(income: Income): Long

    @Update
    suspend fun update(income: Income)

    @Delete
    suspend fun delete(income: Income)
}

@Database(
    entities = [
        ShopConfig::class,
        InventoryItem::class,
        Recipe::class,
        RecipeIngredient::class,
        ProductionLog::class,
        PurchaseLog::class,
        SaleLog::class,
        Customer::class,
        Supplier::class,
        Expense::class,
        Employee::class,
        Income::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shopConfigDao(): ShopConfigDao
    abstract fun inventoryItemDao(): InventoryItemDao
    abstract fun recipeDao(): RecipeDao
    abstract fun recipeIngredientDao(): RecipeIngredientDao
    abstract fun productionLogDao(): ProductionLogDao
    abstract fun purchaseLogDao(): PurchaseLogDao
    abstract fun saleLogDao(): SaleLogDao
    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun incomeDao(): IncomeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sweet_stock_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
