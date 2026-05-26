package com.example.baseapp.ui.base

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View

import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.example.baseapp.data.local.pref.Preference
import com.example.baseapp.ui.component.loading.LoadingManager


abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    protected abstract fun getViewBinding(layoutInflater: LayoutInflater): VB
    protected abstract fun initData(intent: Intent)
    protected abstract fun initView()
    protected abstract fun initAction()
    protected abstract fun initObserver()

    lateinit var binding: VB

    val preference by lazy { Preference.getInstance() }
    private var backPressedTime: Long = 0
    var hasGesture: Boolean = false

    open fun setApplyWindow(){
//        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        ViewCompat.setOnApplyWindowInsetsListener(
            binding.root
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        supportActionBar?.hide()

        binding = getViewBinding(layoutInflater)
        setContentView(binding.root)
        hasGesture = Settings.Secure.getInt(contentResolver, "navigation_mode", 0) == 2
        setApplyWindow()
        LoadingManager.getInstance().initActivity(this)
        initData(intent)
        initView()
        initAction()
        initObserver()

    }

    override fun onStart() {
        super.onStart()
//        AdsManager.getInstance().initial(this)
    }

    override fun onResume() {
        super.onResume()
        LoadingManager.getInstance().initActivity(this)
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    fun showToast(message: String?, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(
            this,
            message,
            duration
        ).show()
    }
}