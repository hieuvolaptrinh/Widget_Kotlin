package com.example.baseapp.ui.page.main

import android.Manifest
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.baseapp.Constants
import com.example.baseapp.R
import com.example.baseapp.data.remote.Resource
import com.example.baseapp.data.remote.dto.WidgetPack
import com.example.baseapp.databinding.ActivityMainBinding
import com.example.baseapp.ui.base.BaseActivity
import com.example.baseapp.ui.receiver.BatteryReceiver
import com.example.baseapp.ui.service.WidgetUpdateService
import com.example.baseapp.ui.widget.WidgetAnimatedProvider
import com.example.baseapp.ui.widget.WidgetAnimatedReviewProvider
import com.example.baseapp.ui.widget.WidgetAnimeGirlProvider
import com.example.baseapp.ui.widget.WidgetCatProvider
import com.example.baseapp.ui.widget.WidgetPackProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ActivityMain : BaseActivity<ActivityMainBinding>(), BatteryReceiver.OnBatteryChangedListener {

    private val viewModel: ViewModelMain by viewModels()
    private var currentPack: WidgetPack? = null

    private val batteryReceiver = BatteryReceiver().apply {
        listener = this@ActivityMain
    }


    private val previewCatJob = listOf(
        R.drawable.img_cat_1,
        R.drawable.img_cat_2,
        R.drawable.img_cat_3,
    )
    private var currentIndex = 0
    private var previewJob: Job? = null


    override fun getViewBinding(layoutInflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun initData(intent: Intent) {
        viewModel.loadWidgetPack("69e4714f9708a69fde4102af")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101
                )
            }
        }

        val serviceIntent = Intent(this, WidgetUpdateService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }


    override fun initView() {
        updateBatteryUi()
        loadAnimatedImage(
            binding.imgAnimated,
            "https://candy-storage.s3.ap-southeast-1.amazonaws.com/themes/uploads/1c846501-5b64-4a37-ae25-32c76ad36150.png"
        )

        loadAnimatedImage(
            binding.imgAnimatedReview,
            "https://candy-storage.s3.ap-southeast-1.amazonaws.com/themes/uploads/1c846501-5b64-4a37-ae25-32c76ad36150.png"
        )
        startPreviewAnimation()

    }


    override fun initAction() {
        binding.cardPreview.setOnClickListener {
            saveWidgetBackgroundUrl()
            pinWidget()
        }
        binding.imgAnimeGirl.setOnClickListener {
            pinAnimeGirl()
        }

        binding.widgetCat.setOnClickListener {
            pinCat()
        }
        binding.imgAnimated.setOnClickListener {
            pinAnimated()
        }
        binding.imgAnimatedReview.setOnClickListener {
            pinAnimatedReview()
        }
    }


    private fun startPreviewAnimation() {

        previewJob?.cancel()

        previewJob = lifecycleScope.launch {

            while (true) {

                Glide.with(this@ActivityMain).load(previewCatJob[currentIndex]).centerCrop()
                    .into(binding.imgCat)

                currentIndex = (currentIndex + 1) % previewCatJob.size

                delay(1000)
            }
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
                    Log.d(
                        "WidgetPack",
                        "Success: name=${result.data.name}, widgets=${result.data.widgets.size}"
                    )
                    // Lưu URL vào SharedPrefs cho widget provider dùng khi pin
                    saveWidgetBackgroundUrl()
                    // Load ảnh nền từ API lên preview card
                    loadPreviewBackground(result.data)
                }
            }
        }
    }

    private fun loadAnimatedImage(imageView: ImageView, url: String) {

        Glide.with(this)
            .load(url)
            // glide-plugin của APNG4Android sẽ tự bắt APNG và render animation.
            .into(imageView)
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, filter)

    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(batteryReceiver)

    }

    // ─── Callback từ BatteryReceiver khi pin thay đổi ───────────────────────────

    override fun onBatteryChanged(percent: Int, isCharging: Boolean) {
        Log.d("ActivityMain", "onBatteryChanged: percent=$percent, isCharging=$isCharging")
        binding.batteryProgress.progress = percent
        binding.imgCharging.visibility = if (isCharging) View.VISIBLE else View.GONE
    }

    // ─── Load ảnh nền từ API vào card preview ──────────────────────────────────

    private fun loadPreviewBackground(pack: WidgetPack) {
        // Lấy backgroundUrl từ widget đầu tiên (widget type "date") → fallback pack.background → pack.preview
        val bgUrl =
            pack.widgets.firstOrNull()?.imageUrl?.firstOrNull()?.backgroundUrl?.takeIf { it.isNotBlank() }
                ?: pack.background.takeIf { it.isNotBlank() }
                ?: pack.preview.takeIf { it.isNotBlank() }

        if (bgUrl != null) {
            Log.d("WidgetPack", "Loading preview background: $bgUrl")
            Glide.with(this).load(bgUrl).centerCrop().into(binding.imgPreview)
        } else {
            Log.w("WidgetPack", "No background URL found")
        }
    }

    // ─── Pin widget lên home screen ─────────────────────────────────────────────

    private fun pinWidget() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val provider = ComponentName(this, WidgetPackProvider::class.java)
        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            val callbackIntent = Intent(
                this, WidgetPackProvider::class.java
            ).setAction(WidgetPackProvider.ACTION_PINNED)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val successCallback = PendingIntent.getBroadcast(this, 0, callbackIntent, flags)
            appWidgetManager.requestPinAppWidget(provider, null, successCallback)
        } else {
            showToast("Launcher không hỗ trợ pin widget")
        }
    }

    private fun pinAnimeGirl() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val provider = ComponentName(this, WidgetAnimeGirlProvider::class.java)
        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            val callbackIntent =
                Intent(this, WidgetAnimeGirlProvider::class.java).setAction(Constants.ACTION_PINNED)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val successCallback = PendingIntent.getBroadcast(this, 0, callbackIntent, flags)
            appWidgetManager.requestPinAppWidget(provider, null, successCallback)
        } else {
            showToast("Launcher không hỗ trợ pin widget")
        }
    }

    private fun pinAnimated() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val provider = ComponentName(this, WidgetAnimatedProvider::class.java)
        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            val callbackIntent =
                Intent(this, WidgetAnimatedProvider::class.java).setAction(Constants.ACTION_PINNED)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val successCallback = PendingIntent.getBroadcast(this, 0, callbackIntent, flags)
            appWidgetManager.requestPinAppWidget(provider, null, successCallback)
        } else {
            showToast("Launcher không hỗ trợ pin widget")
        }
    }

    private fun pinAnimatedReview() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val provider = ComponentName(this, WidgetAnimatedReviewProvider::class.java)
        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            val callbackIntent =
                Intent(
                    this,
                    WidgetAnimatedReviewProvider::class.java
                ).setAction(Constants.ACTION_PINNED)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val successCallback = PendingIntent.getBroadcast(this, 0, callbackIntent, flags)
            appWidgetManager.requestPinAppWidget(provider, null, successCallback)
        } else {
            showToast("Launcher không hỗ trợ pin widget")
        }
    }

    private fun pinCat() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val provider = ComponentName(this, WidgetCatProvider::class.java)
        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            val callbackIntent =
                Intent(this, WidgetCatProvider::class.java).setAction(Constants.ACTION_PINNED)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val successCallback = PendingIntent.getBroadcast(this, 0, callbackIntent, flags)
            appWidgetManager.requestPinAppWidget(provider, null, successCallback)
        } else {
            showToast("Launcher không hỗ trợ pin widget")
        }
    }

    // ─── Cập nhật pin thực (lần đầu mở Activity) ───────

    private fun updateBatteryUi() {
        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging =
            status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        binding.imgCharging.visibility = if (isCharging) View.VISIBLE else View.GONE

//        lấy phần trăm pin để tính toán % của progress bar
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        binding.batteryProgress.progress = batteryLevel
    }

    // ─── Lưu background URL cho WidgetPackProvider ──────────────────────────────

    private fun saveWidgetBackgroundUrl() {
        val pack = currentPack ?: return
        val bgUrl =
            pack.widgets.firstOrNull()?.imageUrl?.firstOrNull()?.backgroundUrl?.takeIf { it.isNotBlank() }
                ?: pack.background.takeIf { it.isNotBlank() }
                ?: pack.preview.takeIf { it.isNotBlank() }

        if (bgUrl != null) {

            WidgetPackProvider.saveBackgroundUrl(this, bgUrl)
        } else {
            Log.w("WidgetPack", "No background URL found in widget pack")
        }
    }

    override fun onDestroy() {
        previewJob?.cancel()
        super.onDestroy()
    }
}