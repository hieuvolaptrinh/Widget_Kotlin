package com.example.baseapp.ui.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import com.example.baseapp.R
import com.example.baseapp.ui.page.main.ActivityMain
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread

class WidgetPackProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        // ✅ Fix 1: Gọi super trước để AppWidgetProvider xử lý các action hệ thống
        // (ACTION_APPWIDGET_UPDATE, ENABLED, DELETED...) qua onUpdate/onEnabled/onDisabled
        super.onReceive(context, intent)

        Log.d("Widget", "onReceive action = ${intent.action}")

        // Chỉ dùng goAsync cho các action của mình
        when (intent.action) {
            ACTION_PINNED, ACTION_MINUTE_TICK -> {
                val pendingResult = goAsync()
                thread {
                    try {
                        Log.d("Widget", "handleReceive: ${intent.action}")
                        val manager = AppWidgetManager.getInstance(context)
                        val provider = ComponentName(context, WidgetPackProvider::class.java)
                        val ids = manager.getAppWidgetIds(provider)
                        for (id in ids) {
                            updateAppWidget(context, manager, id)
                        }
                        scheduleNextMinute(context)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d("Widget", "onUpdate ids=${appWidgetIds.toList()}")
        for (id in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, id)
        }
        scheduleNextMinute(context)
    }

    // ✅ Fix 2: Dùng onEnabled/onDisabled (callback chuẩn của AppWidgetProvider)
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d("Widget", "onEnabled → scheduleNextMinute")
        scheduleNextMinute(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d("Widget", "onDisabled → cancelSchedule")
        cancelSchedule(context)
    }

    companion object {
        const val ACTION_PINNED = "com.example.baseapp.widget.ACTION_PINNED"
        const val ACTION_MINUTE_TICK = "com.example.baseapp.widget.ACTION_MINUTE_TICK"
        private const val PREFS_NAME = "widget_pack_prefs"
        private const val KEY_BACKGROUND_URL = "background_url"
        private const val ALARM_REQUEST_CODE = 1001

        // ─── AlarmManager ────────────────────────────────────────────────────────

        fun scheduleNextMinute(context: Context) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pi = getMinutePendingIntent(context)
            val nextMinute = ((System.currentTimeMillis() / 60_000L) + 1) * 60_000L

            // ✅ Fix 3: Log để debug AlarmManager
            Log.d("Widget", "scheduleNextMinute at ${Date(nextMinute)}")

            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    // ✅ Fix 4: Log canScheduleExactAlarms
                    val canExact = am.canScheduleExactAlarms()
                    Log.d("Widget", "canScheduleExactAlarms = $canExact")
                    if (canExact) {
                        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextMinute, pi)
                    } else {
                        // Fallback: setWindow không cần permission
                        am.setWindow(AlarmManager.RTC_WAKEUP, nextMinute, 30_000L, pi)
                    }
                }
                else -> am.setExact(AlarmManager.RTC_WAKEUP, nextMinute, pi)
            }
        }

        private fun cancelSchedule(context: Context) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.cancel(getMinutePendingIntent(context))
        }

        private fun getMinutePendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, WidgetPackProvider::class.java)
                .setAction(ACTION_MINUTE_TICK)
            return PendingIntent.getBroadcast(
                context, ALARM_REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        // ─── SharedPrefs ─────────────────────────────────────────────────────────

        fun saveBackgroundUrl(context: Context, url: String) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_BACKGROUND_URL, url).apply()
        }

        private fun getBackgroundUrl(context: Context): String? =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_BACKGROUND_URL, null)

        // ─── Update widget ────────────────────────────────────────────────────────

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            // ✅ Log để xác nhận updateAppWidget được gọi
            Log.d("Widget", "updateAppWidget ${getCurrentTime()}")

            val views = RemoteViews(context.packageName, R.layout.widget_date_battery)

            views.setTextViewText(R.id.tvTime, getCurrentTime())
            views.setTextViewText(R.id.tvWeekDay, getCurrentDay())
            views.setTextViewText(R.id.tvMonthDay, getCurrentMonthDay())

            val battery = getBatteryPercent(context)
            views.setProgressBar(R.id.batteryProgress, 100, battery, false)
            views.setTextViewText(R.id.batteryPercent, "$battery%")
            views.setTextColor(
                R.id.batteryPercent, when {
                    battery <= 20 -> android.graphics.Color.parseColor("#FF4444")
                    battery <= 50 -> android.graphics.Color.parseColor("#FFA500")
                    else -> android.graphics.Color.parseColor("#FF0000")
                }
            )

            val pi = PendingIntent.getActivity(
                context, 0,
                Intent(context, ActivityMain::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetRoot, pi)

            // Hiển thị giờ/pin trước (đồng bộ)
            appWidgetManager.updateAppWidget(appWidgetId, views)

            // Download ảnh đồng bộ (đang chạy trong background thread từ goAsync)
            val bgUrl = getBackgroundUrl(context)
            Log.d("Widget", "bgUrl = $bgUrl")
            if (!bgUrl.isNullOrBlank()) {
                try {
                    val connection = URL(bgUrl).openConnection()
                    connection.connectTimeout = 10_000
                    connection.readTimeout = 15_000
                    val bitmap = BitmapFactory.decodeStream(connection.getInputStream())
                    if (bitmap != null) {
                        views.setImageViewBitmap(R.id.imgWidgetBackground, bitmap)
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                        Log.d("Widget", "✅ Background loaded: $appWidgetId")
                    } else {
                        Log.w("Widget", "⚠️ Bitmap null sau decode")
                    }
                } catch (e: Exception) {
                    Log.e("Widget", "❌ Lỗi load hình: ${e.message}")
                }
            } else {
                Log.w("Widget", "⚠️ bgUrl null/blank")
            }
        }

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