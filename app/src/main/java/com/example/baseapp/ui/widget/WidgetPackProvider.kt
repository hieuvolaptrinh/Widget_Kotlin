package com.example.baseapp.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.BatteryManager
import android.util.Log
import android.widget.RemoteViews
import com.example.baseapp.R
import com.example.baseapp.ui.page.main.ActivityMain
import com.example.baseapp.ui.service.WidgetUpdateService
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread

class WidgetPackProvider : AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdateService.start(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WidgetUpdateService.stop(context)
    }

    override fun onUpdate(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray
    ) {
        WidgetUpdateService.start(context)
        thread {
            for (id in appWidgetIds) updateAppWidgetWithImage(context, appWidgetManager, id)
        }
    }

    companion object {
        // để tối ưu thay đổi những gì cần thay đổi thôi không update hết
        private var cachedViews: RemoteViews? = null
        private var cachedBitmap: Bitmap? = null
        private val lastStates = mutableMapOf<Int, Pair<String, Int>>()

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

        private fun getCacheFile(context: Context) = File(context.cacheDir, BG_CACHE_FILE)

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

        // ─── Update giờ/pin (gọi từ Service mỗi phút) ────────────────────────────

        fun updateTimeAndBattery(
            context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int
        ) {
            val time = getCurrentTime()
            val battery = getBatteryPercent(context)
            val currentState = Pair(time, battery)

            // Bỏ qua nếu không có gì thay đổi
            if (lastStates[appWidgetId] == currentState) {
                return
            }
            lastStates[appWidgetId] = currentState

            val views =
                cachedViews ?: RemoteViews(context.packageName, R.layout.widget_date_battery).also {
                    cachedViews = it
                }

            views.setTextViewText(R.id.tvTime, time)
            views.setTextViewText(R.id.tvWeekDay, getCurrentDay())
            views.setTextViewText(R.id.tvMonthDay, getCurrentMonthDay())

            views.setProgressBar(R.id.batteryProgress, 100, battery, false)
            views.setTextViewText(R.id.batteryPercent, "$battery%")
            views.setTextColor(
                R.id.batteryPercent, when {
                    battery <= 20 -> android.graphics.Color.parseColor("#FF4444")
                    battery <= 50 -> android.graphics.Color.parseColor("#FFA500")
                    else -> android.graphics.Color.parseColor("#4CAF50")
                }
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
            val time = getCurrentTime()
            val battery = getBatteryPercent(context)
            lastStates[appWidgetId] = Pair(time, battery)

            Log.d("Widget", "updateAppWidgetWithImage $time")
            val views = RemoteViews(context.packageName, R.layout.widget_date_battery)

            views.setTextViewText(R.id.tvTime, time)
            views.setTextViewText(R.id.tvWeekDay, getCurrentDay())
            views.setTextViewText(R.id.tvMonthDay, getCurrentMonthDay())

            views.setProgressBar(R.id.batteryProgress, 100, battery, false)
            views.setTextViewText(R.id.batteryPercent, "$battery%")
            views.setTextColor(
                R.id.batteryPercent, when {
                    battery <= 20 -> android.graphics.Color.parseColor("#FF4444")
                    battery <= 50 -> android.graphics.Color.parseColor("#FFA500")
                    else -> android.graphics.Color.parseColor("#4CAF50")
                }
            )

            views.setOnClickPendingIntent(R.id.widgetRoot, getLaunchPendingIntent(context))
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

        // ─── Helpers ─────────────────────────────────────────────────────────────

        private fun getLaunchPendingIntent(context: Context) = PendingIntent.getActivity(
            context,
            0,
            Intent(context, ActivityMain::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        private fun getCurrentTime(): String =
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        private fun getCurrentDay(): String =
            SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())

        private fun getCurrentMonthDay(): String =
            SimpleDateFormat("MMMM d", Locale.getDefault()).format(Date())

        private fun getBatteryPercent(context: Context): Int {
            val intent = context.registerReceiver(
                null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            return if (level >= 0 && scale > 0) (level * 100 / scale) else 0
        }
    }
}