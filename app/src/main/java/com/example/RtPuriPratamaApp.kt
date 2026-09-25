package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.RtRepository

class RtPuriPratamaApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: RtRepository by lazy { RtRepository(database) }

    override fun onCreate() {
        super.onCreate()
    }
}
