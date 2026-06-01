package com.example.baseapp.ui.registerReceiver

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.example.baseapp.ui.service.WidgetUpdateService
import com.example.baseapp.ui.widget.WidgetPackProvider

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Kiểm tra có widget nào đang active không
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, WidgetPackProvider::class.java)
            )
            if (ids.isNotEmpty()) {
                WidgetUpdateService.start(context)
            }
        }
    }
}