package com.example.baseapp.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.baseapp.R
import kotlin.concurrent.thread
import com.example.baseapp.Constants.ACTION_PINNED
class WidgetAnimeGirlProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        when (intent?.action) {
            ACTION_PINNED -> {
                val pendingResult = goAsync()

                thread {
                    try {
                        val manager = AppWidgetManager.getInstance(context)
                        val provider = ComponentName(context!!, WidgetAnimeGirlProvider::class.java)
                        val ids = manager.getAppWidgetIds(provider)
                        for (id in ids) updateAppWidgetWithImage(context, manager, id)
                    } catch (e: Exception) {

                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    companion object {


        fun updateAppWidgetWithImage(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_anime_girl)
            appWidgetManager.updateAppWidget(appWidgetId, views)

        }
    }
}