package com.rustam.quizapp

import android.app.Application
import com.rustam.quizapp.data.db.LegacyMigration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application entry point. Runs the one-time import of legacy DataStore progress into
 * Room on a background scope before the UI starts touching the repositories.
 */
class QuizApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            runCatching { LegacyMigration.runOnce(this@QuizApplication) }
        }
    }
}
