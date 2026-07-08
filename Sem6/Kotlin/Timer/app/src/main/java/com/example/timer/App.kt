package com.example.timerapp

import android.app.Application
import android.content.Context
import com.example.timerapp.data.db.AppDatabase
import com.example.timerapp.data.repository.SequenceRepository
import com.example.timerapp.utils.LocaleHelper

class App : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { SequenceRepository(database.sequenceDao()) }

    override fun onCreate() {
        super.onCreate()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(base))
    }
}
