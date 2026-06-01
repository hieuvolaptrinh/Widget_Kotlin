package com.example.baseapp.ui.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.baseapp.R
import com.example.baseapp.ui.widget.WidgetPackProvider

class WidgetUpdateService : Service() {

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Lắng nghe thay đổi thời gian/nguồn điện để cập nhật lại widget.
            updateAllWidgets(context)

        }
    }

    // Cập nhật thời gian/pin cho tất cả widget đang hoạt động.

    private fun updateAllWidgets(context: Context) {

        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(
            ComponentName(context, WidgetPackProvider::class.java)
        )

        for (id in ids) {
            WidgetPackProvider.Companion.updateTimeAndBattery(context, manager, id)
        }
    }


    // Khởi chạy foreground service, đăng ký các sự kiện cập nhật và refresh lần đầu.
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIF_ID, buildNotification())
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(receiver, filter)
        // update lần đầu tiên
        updateAllWidgets(this)

        return START_STICKY // tự restart nếu bị kill
    }

    // Hủy đăng ký receiver khi service dừng.
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    // Service này không hỗ trợ bind.
    override fun onBind(intent: Intent?) = null


    // Tạo notification tối giản cho foreground service (bắt buộc trên Android mới).
    private fun buildNotification(): Notification {
        val channelId = "widget_service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Widget Service",
                NotificationManager.IMPORTANCE_MIN
            ).apply { setShowBadge(false) }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Widget đang chạy")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSilent(true)
            .build()
    }

    companion object {
        private const val NOTIF_ID = 1001

        // Khởi chạy service theo đúng chế độ của từng phiên bản Android.
        fun start(context: Context) {
            val intent = Intent(context, WidgetUpdateService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        // Dừng service cập nhật widget.
        fun stop(context: Context) {
            context.stopService(Intent(context, WidgetUpdateService::class.java))
        }
    }
}
