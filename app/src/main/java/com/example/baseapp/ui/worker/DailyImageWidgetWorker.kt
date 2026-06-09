package com.example.baseapp.ui.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.baseapp.ui.widget.WidgetDailyImageProvider

class DailyImageWidgetWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            WidgetDailyImageProvider.updateAll(
                context = applicationContext,
                randomNewImage = true
            )
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
