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
import android.widget.RemoteViews
import com.example.baseapp.R
import com.example.baseapp.ui.page.main.ActivityMain
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread

class WidgetPackProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_PINNED) {
            val manager = AppWidgetManager.getInstance(context)
            val provider = ComponentName(context, WidgetPackProvider::class.java)
            val ids = manager.getAppWidgetIds(provider)
            for (id in ids) {
                updateAppWidget(context, manager, id)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        const val ACTION_PINNED = "com.example.baseapp.widget.ACTION_PINNED"
        private const val PREFS_NAME = "widget_pack_prefs"
        private const val KEY_BACKGROUND_URL = "background_url"

        /**
         * Save the background URL from API so the widget provider can load it later.
         */
        fun saveBackgroundUrl(context: Context, url: String) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_BACKGROUND_URL, url)
                .apply()
        }

        private fun getBackgroundUrl(context: Context): String? {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_BACKGROUND_URL, null)
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_date_battery)

            // Set time/date text
            val now = Date()
            val timeText = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
            val dayText = SimpleDateFormat("EEEE", Locale.getDefault()).format(now)
            val monthDayText = SimpleDateFormat("MMMM d", Locale.getDefault()).format(now)

            views.setTextViewText(R.id.tvTime, timeText)
            views.setTextViewText(R.id.tvWeekDay, dayText)
            views.setTextViewText(R.id.tvMonthDay, monthDayText)

            // Battery info
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val percent = if (level >= 0 && scale > 0) (level * 100 / scale) else 0

            views.setProgressBar(R.id.batteryProgress, 100, percent, false)
            views.setTextViewText(R.id.batteryPercent, "$percent%")

            // Launch intent
            val launchIntent = Intent(context, ActivityMain::class.java)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, flags)
            views.setOnClickPendingIntent(R.id.widgetRoot, pendingIntent)

            // Update widget with text first (so it's not blank while loading image)
            appWidgetManager.updateAppWidget(appWidgetId, views)

            // Load background image from URL in a background thread
            val bgUrl = getBackgroundUrl(context)
            if (!bgUrl.isNullOrBlank()) {
                thread {
                    try {
                        val bitmap = downloadBitmap(bgUrl)
                        if (bitmap != null) {
                            views.setImageViewBitmap(R.id.imgWidgetBackground, bitmap)
                            appWidgetManager.updateAppWidget(appWidgetId, views)
                            Log.d("WidgetPack", "Background image loaded for widget $appWidgetId")
                        }
                    } catch (e: Exception) {
                        Log.e("WidgetPack", "Failed to load background image: ${e.message}")
                    }
                }
            }
        }

        private fun downloadBitmap(urlString: String): Bitmap? {
            var connection: HttpURLConnection? = null
            return try {
                val url = URL(urlString)
                connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.doInput = true
                connection.connect()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    BitmapFactory.decodeStream(inputStream)
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("WidgetPack", "downloadBitmap error: ${e.message}")
                null
            } finally {
                connection?.disconnect()
            }
        }

        fun requestPin(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val provider = ComponentName(context, WidgetPackProvider::class.java)
            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                appWidgetManager.requestPinAppWidget(provider, null, null)
            }
        }
    }
}
