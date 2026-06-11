package com.example.baseapp.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.baseapp.R
import com.example.baseapp.ui.page.main.ActivityMain
import com.example.baseapp.ui.service.CatStackWidgetService

class WidgetCatStackProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        if (context == null || appWidgetManager == null || appWidgetIds == null) return

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_cat_stack)

            val serviceIntent = Intent(context, CatStackWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                // Mỗi widget cần data Uri riêng để launcher không dùng lại adapter cũ.
                data = android.net.Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }

            // Gắn StackView với RemoteViewsService để cấp dữ liệu cho widget.
            views.setRemoteAdapter(R.id.stackViewCat, serviceIntent)

            val clickIntent = Intent(context, ActivityMain::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }

            val clickPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )

            // Tạo template click, từng item sẽ gắn thêm dữ liệu riêng trong Factory.
            views.setPendingIntentTemplate(R.id.stackViewCat, clickPendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.stackViewCat)
        }
    }

    companion object {
        const val EXTRA_CAT_POSITION = "extra_cat_position"
        const val EXTRA_CAT_TITLE = "extra_cat_title"
    }
}
