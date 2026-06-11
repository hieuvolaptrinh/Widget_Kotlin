package com.example.baseapp.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.baseapp.R
import com.example.baseapp.ui.worker.DailyImageWidgetWorker
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class WidgetDailyImageProvider : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action in timeChangedActions) {
            schedule(context.applicationContext)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)

        // Khi widget đầu tiên được add lên màn hình
        schedule(context.applicationContext)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        // Update ngay khi widget được add / launcher yêu cầu update
        updateAll(context.applicationContext)

        // Đảm bảo lịch WorkManager đã được setup
        schedule(context.applicationContext)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)

        // Khi widget cuối cùng bị xóa, hủy work cho sạch
        cancel(context.applicationContext)
    }

    companion object {
        private const val WORK_NAME = "daily_image_widget_update"
        private const val PREFS_NAME = "daily_image_widget"
        private const val KEY_IMAGE_RES = "image_res"
        private const val UPDATE_HOUR = 16
        private const val UPDATE_MINUTE = 35

        private val images = intArrayOf(
            R.drawable.img_cat_1,
            R.drawable.img_cat_2,
            R.drawable.img_cat_3
        )

        private val timeChangedActions = setOf(
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED
        )

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<DailyImageWidgetWorker>(
                1,
                TimeUnit.DAYS
            )
                .setInitialDelay(calculateInitialDelayMillis(), TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
            Log.d("Scheduler", "thay đổi lịch")
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        private fun calculateInitialDelayMillis(): Long {
            val now = LocalDateTime.now()
            var nextRun = now.toLocalDate().atTime(LocalTime.of(UPDATE_HOUR, UPDATE_MINUTE))

            if (now.isAfter(nextRun)) {
                nextRun = nextRun.plusDays(1)
            }

            return Duration.between(now, nextRun).toMillis()

        }

        fun updateAll(context: Context, randomNewImage: Boolean = false) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(
                context,
                WidgetDailyImageProvider::class.java
            )
            val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (widgetIds.isEmpty()) return

            val imageRes = if (randomNewImage) {
                getRandomImage(context)
            } else {
                getCurrentImage(context)
            }
            val views = RemoteViews(
                context.packageName,
                R.layout.widget_7h_morning
            )
            views.setImageViewResource(R.id.imgDaily, imageRes)
            appWidgetManager.updateAppWidget(widgetIds, views)
            Log.d("Widget", "update new img")
        }

        private fun getCurrentImage(context: Context): Int {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedImage = prefs.getInt(KEY_IMAGE_RES, 0)
            if (savedImage in images) return savedImage

            return getRandomImage(context)
        }

        private fun getRandomImage(context: Context): Int {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val currentImage = prefs.getInt(KEY_IMAGE_RES, 0)
            val nextImage = images
                .filter { it != currentImage }
                .random(Random.Default)

            prefs.edit()
                .putInt(KEY_IMAGE_RES, nextImage)
                .apply()

            return nextImage
        }
    }
}
