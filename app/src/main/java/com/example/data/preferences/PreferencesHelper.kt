package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sweet_stock_prefs")

class PreferencesHelper(private val context: Context) {

    companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val SELECTED_LANGUAGE = stringPreferencesKey("language")
        val TAX_PERCENT = stringPreferencesKey("tax_percent")
        val NOTIFICATION_LOW_STOCK = booleanPreferencesKey("notif_low_stock")
        val NOTIFICATION_EXPIRY = booleanPreferencesKey("notif_expiry")
        val NOTIFICATION_PAYMENTS = booleanPreferencesKey("notif_payments")
        val BENTO_CARDS_ORDER = stringPreferencesKey("bento_cards_order")
        val BENTO_CARDS_VISIBILITY = stringPreferencesKey("bento_cards_visibility")
        
        const val DEFAULT_BENTO_ORDER = "today_sales,net_profit,expenses,inventory_status,low_stock,smart_forecast,quick_actions,monthly_sales,monthly_profit,customer_unpaid,supplier_unpaid,sales_channels,best_least_sellers"
    }

    val bentoCardsOrder: Flow<List<String>> = context.dataStore.data
        .map { preferences ->
            val orderStr = preferences[BENTO_CARDS_ORDER] ?: DEFAULT_BENTO_ORDER
            orderStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }

    suspend fun setBentoCardsOrder(order: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[BENTO_CARDS_ORDER] = order.joinToString(",")
        }
    }

    val bentoCardsVisibility: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            val visibilityStr = preferences[BENTO_CARDS_VISIBILITY] ?: DEFAULT_BENTO_ORDER
            visibilityStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        }

    suspend fun setBentoCardsVisibility(visibleCards: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[BENTO_CARDS_VISIBILITY] = visibleCards.joinToString(",")
        }
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }

    val selectedLanguage: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[SELECTED_LANGUAGE] ?: "English"
        }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_LANGUAGE] = language
        }
    }

    val taxPercent: Flow<Double> = context.dataStore.data
        .map { preferences ->
            (preferences[TAX_PERCENT] ?: "5.0").toDoubleOrNull() ?: 5.0
        }

    suspend fun setTaxPercent(percent: Double) {
        context.dataStore.edit { preferences ->
            preferences[TAX_PERCENT] = percent.toString()
        }
    }

    val lowStockNotif: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[NOTIFICATION_LOW_STOCK] ?: true }

    suspend fun setLowStockNotif(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[NOTIFICATION_LOW_STOCK] = enabled }
    }

    val expiryNotif: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[NOTIFICATION_EXPIRY] ?: true }

    suspend fun setExpiryNotif(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[NOTIFICATION_EXPIRY] = enabled }
    }

    val paymentsNotif: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[NOTIFICATION_PAYMENTS] ?: true }

    suspend fun setPaymentsNotif(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[NOTIFICATION_PAYMENTS] = enabled }
    }
}
