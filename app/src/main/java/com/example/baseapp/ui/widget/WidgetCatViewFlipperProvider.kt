package com.example.baseapp.ui.widget


import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import com.example.baseapp.R

class WidgetCatViewFlipperProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        val safeContext = context ?: return
        val safeAppWidgetManager = appWidgetManager ?: return

        appWidgetIds?.forEach { appWidgetId ->
            updateWidget(
                context = safeContext,
                appWidgetManager = safeAppWidgetManager,
                appWidgetId = appWidgetId
            )
        }
    }

    override fun onRestored(
        context: Context?, oldWidgetIds: IntArray?, newWidgetIds: IntArray?
    ) {
        super.onRestored(context, oldWidgetIds, newWidgetIds)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }

    companion object {
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {

            val views = RemoteViews(
                context.packageName, R.layout.widget_cat_view_flipper
            )

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
