package com.example.baseapp.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.BatteryManager
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.example.baseapp.R
import com.example.baseapp.ui.page.main.ActivityMain
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlin.concurrent.thread

class WidgetPackProvider : AppWidgetProvider() {



    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Log.d("Widget", "onReceive action = ${intent.action}")

        when (intent.action) {
            ACTION_PINNED -> {
                val pendingResult = goAsync()
                thread {
                    try {
                        val manager = AppWidgetManager.getInstance(context)
                        val provider = ComponentName(context, WidgetPackProvider::class.java)
                        val ids = manager.getAppWidgetIds(provider)
                        for (id in ids) updateAppWidgetWithImage(context, manager, id)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            Intent.ACTION_POWER_CONNECTED, Intent.ACTION_POWER_DISCONNECTED -> {
                val pendingResult = goAsync()
                thread {
                    try {
                        val manager = AppWidgetManager.getInstance(context)
                        val provider = ComponentName(context, WidgetPackProvider::class.java)
                        val ids = manager.getAppWidgetIds(provider)

                        // Xác định trạng thái sạc trực tiếp từ action
                        val isCharging = intent.action == Intent.ACTION_POWER_CONNECTED
                        val batteryInfo = getBatteryInfo(context)

                        Log.d(
                            "Widget",
                            "Power event: charging=$isCharging, percent=${batteryInfo.percent}"
                        )

                        for (id in ids) {
                            updateBattery(context, manager, id, batteryInfo.percent, isCharging)
                        }
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    override fun onUpdate(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray
    ) {
        thread {
            for (id in appWidgetIds) updateAppWidgetWithImage(context, appWidgetManager, id)
        }
    }

    companion object {
        private var cachedBitmap: Bitmap? = null

        const val ACTION_PINNED = "com.example.baseapp.widget.ACTION_PINNED"
        private const val PREFS_NAME = "widget_pack_prefs"
        private const val KEY_BACKGROUND_URL = "background_url"
        private const val BG_CACHE_FILE = "widget_bg_cache.png"

        // ─── SharedPrefs ─────────────────────────────────────────────────────────

        fun saveBackgroundUrl(context: Context, url: String) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putString(KEY_BACKGROUND_URL, url).apply()
        }

        private fun getBackgroundUrl(context: Context): String? =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_BACKGROUND_URL, null)

        // ─── Cache file ───────────────────────────────────────────────────────────

        private fun getCacheFile(context: Context) = File(context.filesDir, BG_CACHE_FILE)

        private fun loadCachedBitmap(context: Context): Bitmap? {
            if (cachedBitmap != null) return cachedBitmap

            val file = getCacheFile(context)
            if (!file.exists()) return null
            return BitmapFactory.decodeFile(file.absolutePath).also {
                cachedBitmap = it
                Log.d("Widget", if (it != null) " Loaded from cache" else " Cache decode failed")
            }
        }

        private fun downloadAndCacheBitmap(context: Context, url: String): Bitmap? {
            return try {
                Log.d("Widget", "Downloading image...")
                val conn = URL(url).openConnection()
                conn.connectTimeout = 10_000
                conn.readTimeout = 15_000
                val bitmap = BitmapFactory.decodeStream(conn.getInputStream())
                if (bitmap != null) {
                    FileOutputStream(getCacheFile(context)).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
                    }
                    cachedBitmap = bitmap
                    Log.d("Widget", " Image downloaded & cached")
                }
                bitmap
            } catch (e: Exception) {
                Log.e("Widget", " Download error: ${e.message}")
                null
            }
        }

        // ─── Update pin (gọi từ Service mỗi phút hoặc khi có event sạc) ──────────

        fun updateBattery(context: Context, percent: Int, isCharging: Boolean) {
            val manager = AppWidgetManager.getInstance(context)
            val provider = ComponentName(context, WidgetPackProvider::class.java)
            val ids = manager.getAppWidgetIds(provider)
            for (id in ids) updateBattery(context, manager, id, percent, isCharging)
        }

        private fun updateBattery(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            percent: Int,
            isCharging: Boolean
        ) {
            // Luôn tạo mới RemoteViews (không cache — tránh tích lũy operations)
            val views = RemoteViews(context.packageName, R.layout.widget_date_battery)

            Log.d("Widget", "updateBattery: percent=$percent, isCharging=$isCharging")

            views.setProgressBar(R.id.batteryProgress, 100, percent.coerceIn(0, 100), false)
            views.setViewVisibility(
                R.id.imgCharging,
                if (isCharging) View.VISIBLE else View.GONE
            )

            views.setOnClickPendingIntent(R.id.widgetRoot, getLaunchPendingIntent(context))

            val cached = loadCachedBitmap(context)
            if (cached != null) {
                views.setImageViewBitmap(R.id.imgWidgetBackground, cached)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }


        // ─── Update đầy đủ + download ảnh (lần đầu) ──────────────────────────────

        fun updateAppWidgetWithImage(
            context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int
        ) {
            val batteryInfo = getBatteryInfo(context)

            val views = RemoteViews(context.packageName, R.layout.widget_date_battery)

            views.setProgressBar(R.id.batteryProgress, 100, batteryInfo.percent, false)
            views.setViewVisibility(
                R.id.imgCharging,
                if (batteryInfo.isCharging) View.VISIBLE else View.GONE
            )

            appWidgetManager.updateAppWidget(appWidgetId, views)

            val bgUrl = getBackgroundUrl(context)
            val bitmap =
                loadCachedBitmap(context) ?: if (!bgUrl.isNullOrBlank()) downloadAndCacheBitmap(
                    context,
                    bgUrl
                ) else null

            if (bitmap != null) {
                views.setImageViewBitmap(R.id.imgWidgetBackground, bitmap)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        private data class BatteryInfo(val percent: Int, val isCharging: Boolean)

        private fun getBatteryInfo(context: Context): BatteryInfo {
            val intent = context.registerReceiver(
                null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val percent = if (level >= 0 && scale > 0) {
                ((level * 100f) / scale).toInt().coerceIn(0, 100)
            } else {
                0
            }
            val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
            return BatteryInfo(percent, isCharging)
        }

        // ─── Helpers ─────────────────────────────────────────────────────────────

        private fun getLaunchPendingIntent(context: Context) = PendingIntent.getActivity(
            context,
            0,
            Intent(context, ActivityMain::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}