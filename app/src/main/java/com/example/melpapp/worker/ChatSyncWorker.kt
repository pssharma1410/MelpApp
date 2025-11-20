package com.example.melpapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.melpapp.domain.usecase.RefreshChatsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
class ChatSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val refreshChatsUseCase: RefreshChatsUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val time = refreshChatsUseCase()
            Timber.d("Background sync completed in ${time}ms")

            val request = OneTimeWorkRequestBuilder<ChatSyncWorker>()
                .setInitialDelay(30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(applicationContext)
                .enqueueUniqueWork(
                    "chat_sync_work",
                    ExistingWorkPolicy.REPLACE,
                    request
                )

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Sync failed")
            Result.retry()
        }
    }
}
