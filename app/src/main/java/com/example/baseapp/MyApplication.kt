package com.example.baseapp

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.baseapp.data.local.pref.Preference
import com.example.baseapp.data.local.pref.PreferenceManager
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import java.lang.ref.WeakReference


@HiltAndroidApp
class MyApplication : Application(), Application.ActivityLifecycleCallbacks,
    DefaultLifecycleObserver {
    private var currentActivityRef = WeakReference<Activity>(null)
    private var lastSavedTime = 0L
    private var activityCount = 0

    override fun onCreate() {
        super<Application>.onCreate()

        instance = this
        PreferenceManager.init(applicationContext)
//        IAP.getInstance().initial(this)
//        applySavedMode()

        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
//        MobileAds.initialize(this)

//        AppEventsLogger.activateApp(this)
//        FacebookSdk.setAutoLogAppEventsEnabled(false)
//        FacebookSdk.fullyInitialize()
//        AdsImpressionManager.getInstance(this)
//        SubscriptionLoggerManager.getInstance(this)

//        com.authenticatorapp.adsmob.core.PreferenceManager.init(this)

//        val currentLanguage = Preference.getInstance().language
//        if (currentLanguage == "") {
//            var languageCode = ""
//            val defaultCode = Locale.getDefault().language
//            Preference.getInstance().defaultLanguage = defaultCode
//            Preference.getInstance().isUseSystemLanguage = true
//
//            if (LanguageInstance.getInstance(applicationContext)!!.allLanguage.indexOfLast { it.prefix == defaultCode } >= 0) {
//                languageCode = defaultCode
//            } else {
//                languageCode = "en"
//            }
//            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode))
//            Preference.getInstance().language = languageCode
//        } else {
//            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(currentLanguage))
//        }
//        fetchSaleOff()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

//        val currentTime = System.currentTimeMillis()
//        if(lastSavedTime != 0L && currentTime - lastSavedTime > 10000 &&
//            currentActivityRef.get() !is ActivityLoading && currentActivityRef.get() !is ActivitySplash
//        ) {
//            startActivity(Intent(applicationContext, ActivityLoading::class.java).apply {
//                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            })
//        }

    }

    private fun applySavedMode() {
        val preference = Preference.getInstance()
        val savedMode = preference.savedMode
        AppCompatDelegate.setDefaultNightMode(savedMode)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        lastSavedTime = System.currentTimeMillis()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            clearAppCache()
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        activityCount++
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivityRef = WeakReference(activity)
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
        currentActivityRef.clear()
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        activityCount--
        if (activityCount == 0) {
            clearAppCache()
        }
        currentActivityRef.clear()
    }

    private fun clearAppCache() {
        try {
            val cacheDir = applicationContext.cacheDir
            if (cacheDir != null && cacheDir.isDirectory) {
                deleteDir(cacheDir)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            if (children != null) {
                for (child in children) {
                    val success = deleteDir(File(dir, child))
                    if (!success) {
                        return false
                    }
                }
            }
        } else if(dir!= null && dir.isFile) {
            return dir.delete();
        } else {
            return false;
        }
        return dir.delete()
    }


    private fun getDirSize(dir: File?): Long {
        var size: Long = 0
        if (dir != null && dir.isDirectory) {
            val files = dir.listFiles()
            if (files != null) {
                for (file in files) {
                    size += if (file.isFile) {
                        file.length()
                    } else {
                        getDirSize(file)
                    }
                }
            }
        }
        return size
    }

    private fun fetchSaleOff() {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val saleOffRes = ApiClient.apiSaleOff.getActiveSale()
//
//                if (saleOffRes.isSuccessful) {
//                    val saleOff = saleOffRes.body()
//                    if (saleOff != null) {
//                        val remainingTime = TimeUtils.getRemainingTime(saleOff.endAt)
//                        if (remainingTime != null && !remainingTime.isNegative) {
//                            currentSaleOff = saleOff
//                        }
//                    }
//                }
//            } catch (e: Exception) {
//                currentSaleOff = null
//            }
//        }
    }

    companion object {
//        var isUserClosedGift = false
//        var currentSaleOff: SaleOff? = null

        private var instance: MyApplication? = null
        fun getInstance(): MyApplication {
            return instance!!
        }
    }
}