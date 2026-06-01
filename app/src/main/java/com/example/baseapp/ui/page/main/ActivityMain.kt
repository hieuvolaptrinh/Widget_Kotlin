package com.example.baseapp.ui.page.main

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.example.baseapp.data.remote.Resource
import com.example.baseapp.data.remote.dto.WidgetPack
import com.example.baseapp.databinding.ActivityMainBinding
import com.example.baseapp.ui.base.BaseActivity
import com.example.baseapp.ui.widget.WidgetPackProvider
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class ActivityMain : BaseActivity<ActivityMainBinding>() {

    private val viewModel: ViewModelMain by viewModels()
    private var currentPack: WidgetPack? = null

    override fun getViewBinding(layoutInflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun initData(intent: Intent) {
        viewModel.loadWidgetPack("69e4714f9708a69fde4102af")
    }

    override fun initView() {
        // Hiển thị thời gian & pin thực ngay khi vào màn hình
        updateDateTimeUi()
        updateBatteryUi()
    }

    override fun initAction() {
        binding.cardPreview.setOnClickListener {
            saveWidgetBackgroundUrl()
            pinWidget()
        }
    }

    override fun initObserver() {
        viewModel.widgetPack.observe(this) { result ->
            when (result) {
                is Resource.Loading -> Log.d("WidgetPack", "Loading...")
                is Resource.Error -> {
                    Log.e("WidgetPack", "Error: ${result.message} (code=${result.code})")
                    showToast(result.message)
                }
                is Resource.Success -> {
                    currentPack = result.data
                    Log.d("WidgetPack", "Success: name=${result.data.name}, widgets=${result.data.widgets.size}")
                    // Lưu URL vào SharedPrefs cho widget provider dùng khi pin
                    saveWidgetBackgroundUrl()
                    // Load ảnh nền từ API lên preview card
                    loadPreviewBackground(result.data)
                }
            }
        }
    }

    // ─── Load ảnh nền từ API vào card preview ──────────────────────────────────

    private fun loadPreviewBackground(pack: WidgetPack) {
        // Lấy backgroundUrl từ widget đầu tiên (widget type "date") → fallback pack.background → pack.preview
        val bgUrl = pack.widgets.firstOrNull()?.imageUrl?.firstOrNull()?.backgroundUrl
            ?.takeIf { it.isNotBlank() }
            ?: pack.background.takeIf { it.isNotBlank() }
            ?: pack.preview.takeIf { it.isNotBlank() }

        if (bgUrl != null) {
            Log.d("WidgetPack", "Loading preview background: $bgUrl")
            Glide.with(this)
                .load(bgUrl)
                .centerCrop()
                .into(binding.imgPreview)
        } else {
            Log.w("WidgetPack", "No background URL found")
        }
    }

    // ─── Pin widget lên home screen ─────────────────────────────────────────────

    private fun pinWidget() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val provider = ComponentName(this, WidgetPackProvider::class.java)
        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            val callbackIntent = Intent(this, WidgetPackProvider::class.java)
                .setAction(WidgetPackProvider.ACTION_PINNED)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val successCallback = PendingIntent.getBroadcast(this, 0, callbackIntent, flags)
            appWidgetManager.requestPinAppWidget(provider, null, successCallback)
        } else {
            showToast("Launcher không hỗ trợ pin widget")
        }
    }

    // ─── Cập nhật pin thực ──────────────────────────────────────────────────────

    private fun updateBatteryUi() {
        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val percent = if (level >= 0 && scale > 0) (level * 100 / scale) else 0

        binding.batteryProgress.progress = percent
        binding.batteryPercent.text = "$percent%"

        // Màu text pin theo mức sạc
        binding.batteryPercent.setTextColor(
            when {
                percent <= 20 -> android.graphics.Color.parseColor("#FF4444") // Đỏ
                percent <= 50 -> android.graphics.Color.parseColor("#FFA500") // Cam
                else -> android.graphics.Color.WHITE
            }
        )
    }

    // ─── Cập nhật giờ/ngày thực ─────────────────────────────────────────────────

    private fun updateDateTimeUi() {
        val now = Date()
        binding.tvTime.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
        binding.tvWeekDay.text = SimpleDateFormat("EEEE", Locale.getDefault()).format(now)
        binding.tvMonthDay.text = SimpleDateFormat("MMMM d", Locale.getDefault()).format(now)
    }

    // ─── Lưu background URL cho WidgetPackProvider ──────────────────────────────

    private fun saveWidgetBackgroundUrl() {
        val pack = currentPack ?: return
        val bgUrl = pack.widgets.firstOrNull()?.imageUrl?.firstOrNull()?.backgroundUrl
            ?.takeIf { it.isNotBlank() }
            ?: pack.background.takeIf { it.isNotBlank() }
            ?: pack.preview.takeIf { it.isNotBlank() }

        if (bgUrl != null) {
            Log.d("WidgetPack", "Saving widget background URL: $bgUrl")
            WidgetPackProvider.saveBackgroundUrl(this, bgUrl)
        } else {
            Log.w("WidgetPack", "No background URL found in widget pack")
        }
    }
}