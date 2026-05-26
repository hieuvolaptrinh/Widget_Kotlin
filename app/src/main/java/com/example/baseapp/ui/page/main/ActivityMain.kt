package com.example.baseapp.ui.page.main

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.baseapp.data.remote.Resource
import com.example.baseapp.data.remote.dto.WidgetPack
import com.example.baseapp.databinding.ActivityMainBinding
import com.example.baseapp.ui.base.BaseActivity
import com.example.baseapp.ui.widget.HelloWidgetProvider
import com.example.baseapp.ui.widget.WidgetPackProvider
import dagger.hilt.android.AndroidEntryPoint

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

    }

    fun previewWidgetPack() {
        val pack = currentPack ?: return
        val previewUrl = pack.widgets.getOrNull(1)?.previewUrl
            ?.takeIf { it.isNotBlank() }
            ?: return

        showPreview(previewUrl)
        binding.batteryBody.isVisible = false
        binding.batteryCap.isVisible = false
    }

    override fun initAction() {
        binding.cardPreview.setOnClickListener {
            // Save background URL before pinning so the widget can load it
            saveWidgetBackgroundUrl()

            val appWidgetManager = AppWidgetManager.getInstance(this)
            val provider = ComponentName(this, WidgetPackProvider::class.java)
            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                val callbackIntent = Intent(this, WidgetPackProvider::class.java)
                    .setAction(WidgetPackProvider.ACTION_PINNED)
                val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                val successCallback = PendingIntent.getBroadcast(this, 0, callbackIntent, flags)
                appWidgetManager.requestPinAppWidget(provider, null, successCallback)
            } else {
                showToast("Launcher does not support pinning widgets")
            }
        }

        binding.tvHelloWidget.setOnClickListener {
            val appWidgetManager = AppWidgetManager.getInstance(this)
            val provider = ComponentName(this, HelloWidgetProvider::class.java)
            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                appWidgetManager.requestPinAppWidget(provider, null, null)
            } else {
                showToast("Launcher does not support pinning widgets")
            }
        }
    }

    override fun initObserver() {
        viewModel.widgetPack.observe(this) { result ->
            when (result) {
                is Resource.Loading -> Log.d("WidgetPack", "Loading widget pack...")
                is Resource.Error -> {
                    Log.e("WidgetPack", "Error: ${result.message} (code=${result.code})")
                    showToast(result.message)
                }
                is Resource.Success -> {
                    currentPack = result.data
                    Log.d("WidgetPack", "Success: name=${result.data.name}, widgets=${result.data.widgets.size}")
                    // Save background URL immediately so it's ready when widget is pinned
                    saveWidgetBackgroundUrl()
                    previewWidgetPack()
                }
            }
        }
    }

    private fun showPreview(url: String) {
        Log.d("WidgetPack", "Show preview: $url")
        binding.imgPreview.isVisible = true
        binding.groupInfoText.visibility = View.GONE
        Glide.with(this)
            .load(url)
            .into(binding.imgPreview)
    }

    private fun applySelectedBackground(url: String) {
        binding.imgPreview.isVisible = true
        binding.groupInfoText.visibility = View.VISIBLE
        Glide.with(this)
            .load(url)
            .into(binding.imgPreview)
    }

    private fun updateBatteryUi() {
        val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val percent = if (level >= 0 && scale > 0) (level * 100 / scale) else 0

        binding.batteryProgress.progress = percent
        binding.batteryPercent.text = "$percent%"
    }

    /**
     * Get the best available background URL from the current widget pack and save it
     * to SharedPreferences so the widget provider can load it.
     */
    private fun saveWidgetBackgroundUrl() {
        val pack = currentPack ?: return

        // Try to get backgroundUrl from the first widget's imageUrl list
        val bgUrl = pack.widgets.firstOrNull()?.imageUrl?.firstOrNull()?.backgroundUrl
            ?.takeIf { it.isNotBlank() }
        // Fallback to previewUrl of the first widget
            ?: pack.widgets.firstOrNull()?.previewUrl?.takeIf { it.isNotBlank() }
        // Fallback to pack-level background
            ?: pack.background.takeIf { it.isNotBlank() }
        // Fallback to pack preview
            ?: pack.preview.takeIf { it.isNotBlank() }

        if (bgUrl != null) {
            Log.d("WidgetPack", "Saving widget background URL: $bgUrl")
            WidgetPackProvider.saveBackgroundUrl(this, bgUrl)
        } else {
            Log.w("WidgetPack", "No background URL found in widget pack")
        }
    }
}