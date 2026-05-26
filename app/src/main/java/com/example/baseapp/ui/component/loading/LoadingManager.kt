package com.example.baseapp.ui.component.loading

import androidx.appcompat.app.AppCompatActivity
import com.example.baseapp.ui.component.dialog.DialogLoading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoadingManager {
    companion object{
        @Volatile
        private lateinit var INSTANCE: LoadingManager

        fun getInstance(): LoadingManager {
            if (!Companion::INSTANCE.isInitialized) {
                synchronized(this) {
                    if (!Companion::INSTANCE.isInitialized) {
                        INSTANCE = LoadingManager()
                    }
                }
            }
            return INSTANCE
        }
    }
    private var activity: AppCompatActivity? = null
    private val loadingView: DialogLoading = DialogLoading()

    fun initActivity(activity: AppCompatActivity){
        this.activity = activity

    }
    fun showLoading() {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Main) {
                if (!loadingView.isVisible && !loadingView.isAdded)
                    loadingView.show(activity!!.supportFragmentManager, null)
            }
        }


    }

    fun dismissLoading() {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Main) {
                if (loadingView.dialog?.isShowing == true)
                    loadingView.dismiss()
            }
        }

    }
}