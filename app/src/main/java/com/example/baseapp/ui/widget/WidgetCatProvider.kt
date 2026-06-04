package com.example.baseapp.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import com.example.baseapp.Constants.ACTION_PINNED
import com.example.baseapp.R

class WidgetCatProvider : AppWidgetProvider() {

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        if (intent?.action == ACTION_PINNED && context != null) {
            Log.d("WidgetCatProvider", "Widget pinned, starting scheduler")
            startScheduler(context)
        }
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        context?.let {
            Log.d("WidgetCatProvider", "First widget added, starting scheduler")
            startScheduler(it)
        }
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        Log.d("WidgetCatProvider", "Last widget removed, stopping scheduler")
        stopScheduler()
    }

    override fun onUpdate(
            context: Context?,
            appWidgetManager: AppWidgetManager?,
            appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        context?.let {
            Log.d("WidgetCatProvider", "onUpdate called for ${appWidgetIds?.size} widgets")
            startScheduler(it)
        }
    }

    companion object {

        // Danh sách ảnh mèo gốc
        private val frames =
                listOf(R.drawable.img_cat_1, R.drawable.img_cat_2, R.drawable.img_cat_3)

        private var handler: Handler? = null
        private var runnable: Runnable? = null
        private var currentFrameIndex = 0

        // Public methods để Service có thể gọi
        fun startScheduler(context: Context) {
            stopScheduler() // Dừng scheduler cũ nếu có

            handler = Handler(Looper.getMainLooper())
            runnable =
                    object : Runnable {
                        override fun run() {
                            updateAllCatWidgets(context)
                            handler?.postDelayed(this, 1000) // Lặp lại sau 1 giây
                        }
                    }
            handler?.post(runnable!!)
            Log.d("WidgetCatProvider", "Scheduler started")
        }

        fun stopScheduler() {
            runnable?.let { handler?.removeCallbacks(it) }
            handler = null
            runnable = null
            Log.d("WidgetCatProvider", "Scheduler stopped")
        }

        private fun updateAllCatWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetIds =
                    appWidgetManager.getAppWidgetIds(
                            ComponentName(context, WidgetCatProvider::class.java)
                    )

            if (widgetIds.isEmpty()) {
                Log.d("WidgetCatProvider", "No cat widgets found, stopping scheduler")
                stopScheduler()
                return
            }

            // Cập nhật tất cả widget mèo với frame hiện tại
            widgetIds.forEach { widgetId ->
                updateWidget(context, appWidgetManager, widgetId, currentFrameIndex)
            }

            // Chuyển sang frame tiếp theo
            currentFrameIndex = (currentFrameIndex + 1) % frames.size
            Log.d(
                    "WidgetCatProvider",
                    "Updated ${widgetIds.size} widgets, frame: $currentFrameIndex"
            )
        }

        private fun updateWidget(
                context: Context,
                appWidgetManager: AppWidgetManager,
                appWidgetId: Int,
                frameIndex: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_cat)

            // Hiển thị ảnh mèo gốc trực tiếp
            views.setImageViewResource(R.id.imgCat, frames[frameIndex])

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
