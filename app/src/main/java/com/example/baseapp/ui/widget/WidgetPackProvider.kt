package com.example.baseapp.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.widget.RemoteViews
import com.example.baseapp.R
import com.example.baseapp.ui.page.main.ActivityMain
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_date_battery)

            val now = Date()
            val timeText = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
            val dayText = SimpleDateFormat("EEEE", Locale.getDefault()).format(now)
            val monthDayText = SimpleDateFormat("MMMM d", Locale.getDefault()).format(now)

            views.setTextViewText(R.id.tvTime, timeText)
            views.setTextViewText(R.id.tvWeekDay, dayText)
            views.setTextViewText(R.id.tvMonthDay, monthDayText)

            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val percent = if (level >= 0 && scale > 0) (level * 100 / scale) else 0

            views.setProgressBar(R.id.batteryProgress, 100, percent, false)
            views.setTextViewText(R.id.batteryPercent, "$percent%")

            val launchIntent = Intent(context, ActivityMain::class.java)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, flags)
            views.setOnClickPendingIntent(R.id.widgetRoot, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
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
