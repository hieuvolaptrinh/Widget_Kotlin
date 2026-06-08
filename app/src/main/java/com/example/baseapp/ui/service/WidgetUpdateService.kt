package com.example.baseapp.ui.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.baseapp.ui.receiver.BatteryReceiver
import com.example.baseapp.ui.widget.WidgetAnimatedProvider
import com.example.baseapp.ui.widget.WidgetCatProvider
import com.example.baseapp.ui.widget.WidgetPackProvider

class WidgetUpdateService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        WidgetAnimatedProvider.startScheduler(this)
        // START_STICKY để OS tự khởi động lại service nếu bị kill do thiếu RAM
        return START_STICKY
    }

    private val batteryReceiver =
            BatteryReceiver().apply {
                listener =
                        object : BatteryReceiver.OnBatteryChangedListener {
                            override fun onBatteryChanged(percent: Int, isCharging: Boolean) {
                                // WidgetPackProvider handles updating all instances of the battery
                                // widget
                                WidgetPackProvider.updateBattery(
                                        this@WidgetUpdateService,
                                        percent,
                                        isCharging
                                )
                            }
                        }
            }

    override fun onCreate() {
        super.onCreate()
        startForegroundService()

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, filter)

        WidgetAnimatedProvider.startScheduler(this)
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)

        // Dừng scheduler khi service bị destroy
        WidgetAnimatedProvider.stopScheduler()
        WidgetCatProvider.stopScheduler()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startForegroundService() {
        val channelId = "widget_updater_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                    NotificationChannel(
                            channelId,
                            "Widget Update Service",
                            NotificationManager
                                    .IMPORTANCE_MIN // IMPORTANCE_MIN để giảm thiểu độ phiền phức
                            // của thông báo
                            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        val notification: Notification =
                NotificationCompat.Builder(this, channelId)
                        .setContentTitle("")
                        .setContentText("")
                        .setSmallIcon(com.example.baseapp.R.mipmap.ic_launcher)
                        .setPriority(NotificationCompat.PRIORITY_MIN)
                        .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, notification)
        }
    }
}
