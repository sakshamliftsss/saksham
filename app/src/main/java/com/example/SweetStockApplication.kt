package com.example

import android.app.Application
import com.example.data.database.AppDatabase
import com.example.data.preferences.PreferencesHelper
import com.example.data.repository.SweetStockRepository

class SweetStockApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { SweetStockRepository(database) }
    val preferencesHelper by lazy { PreferencesHelper(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: SweetStockApplication
            private set
    }
}
