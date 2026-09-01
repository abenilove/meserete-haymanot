package com.mezmuretewahedo.app

import android.app.Application
import com.mezmuretewahedo.app.data.HymnDatabase
import com.mezmuretewahedo.app.data.HymnRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MezmurApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val repository: HymnRepository by lazy {
        HymnRepository(HymnDatabase.getInstance(this).hymnDao())
    }

    override fun onCreate() {
        super.onCreate()
        // Seed the database with the bundled hymnal on first launch.
        applicationScope.launch {
            repository.ensureSeeded(this@MezmurApp)
        }
    }
}
