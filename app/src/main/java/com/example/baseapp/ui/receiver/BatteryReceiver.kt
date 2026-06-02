package com.example.baseapp.ui.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.util.Log

class BatteryReceiver : BroadcastReceiver() {

    interface OnBatteryChangedListener {
        fun onBatteryChanged(percent: Int, isCharging: Boolean)
    }

    var listener: OnBatteryChangedListener? = null

    private var lastPercent: Int = -1
    private var lastCharging: Boolean? = null

    override fun onReceive(context: Context, intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val percent = if (level >= 0 && scale > 0) {
            ((level * 100f) / scale).toInt().coerceIn(0, 100)
        } else {
            0
        }

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        // Only notify if something actually changed to avoid spamming updates
        if (percent != lastPercent || isCharging != lastCharging) {
            Log.d("BatteryReceiver", "Battery changed: $percent%, charging=$isCharging")
            lastPercent = percent
            lastCharging = isCharging
            listener?.onBatteryChanged(percent, isCharging)
        }
    }
}