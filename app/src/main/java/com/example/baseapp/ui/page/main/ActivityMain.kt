package com.example.baseapp.ui.page.main

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.baseapp.data.remote.Resource
import com.example.baseapp.data.remote.dto.WidgetPack
import com.example.baseapp.databinding.ActivityMainBinding
import com.example.baseapp.ui.base.BaseActivity
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
        updateBatteryUi()
    }

    override fun initAction() {
        binding.cardPreview.setOnClickListener {
            val selectedUrl = currentPack
                ?.widgets
                ?.firstOrNull()
                ?.imageUrl
                ?.firstOrNull()
                ?.backgroundUrl
                ?.takeIf { it.isNotBlank() }

            if (selectedUrl != null) {
                applySelectedBackground(selectedUrl)
            }
        }
    }

    override fun initObserver() {
        viewModel.widgetPack.observe(this) { result ->
            when (result) {
                is Resource.Loading -> Unit
                is Resource.Error -> showToast(result.message)
                is Resource.Success -> {
                    currentPack = result.data
                    val previewUrl = result.data.widgets.firstOrNull()?.previewUrl
                        ?.takeIf { it.isNotBlank() }
                        ?: result.data.preview

                    if (previewUrl.isNotBlank()) {
                        showPreview(previewUrl)
                    }
                }
            }
        }
    }

    private fun showPreview(url: String) {
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
}