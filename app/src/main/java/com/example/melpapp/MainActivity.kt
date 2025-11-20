package com.example.melpapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.melpapp.ui.navigation.AppNavHost
import com.example.melpapp.worker.ChatSyncWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheduleSync()

        setContent {
            AppRoot()
        }
    }

    private fun scheduleSync() {
        val request = OneTimeWorkRequestBuilder<ChatSyncWorker>()
            .setInitialDelay(30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniqueWork(
                "chat_sync_work",
                ExistingWorkPolicy.REPLACE,
                request
            )
    }
}

@Composable
fun AppRoot() {
    MaterialTheme {
        Surface {
            AppNavHost()
        }
    }
}
