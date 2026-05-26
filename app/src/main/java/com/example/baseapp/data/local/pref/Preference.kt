package com.example.baseapp.data.local.pref

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.example.baseapp.Constants

object PreferenceManager {
    private const val PREFERENCE_NAME = "retake_image_app_preference"
    lateinit var preferences: SharedPreferences
        private set

    fun init(context: Context) {
        preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    }
}

class Preference {
    companion object {
        private const val ON_BOARDING = "onBoarding"
        private const val POSITION_SCREEN = "POSITION_SCREEN"
        private const val RESTORE_COUNT = "RESTORE_COUNT"
        private const val NIGHT_MODE = "NIGHT_MODE"
        private const val LANGUAGE = "LANGUAGE"
        private const val IS_LOADED_LANGUAGE_NATIVE = "IS_LOADED_LANGUAGE_NATIVE"
        private const val IS_USE_SYSTEM_LANGUAGE = "IS_USE_SYSTEM_LANGUAGE"
        private const val DEFAULT_LANGUAGE = "DEFAULT_LANGUAGE"

        @Volatile
        private lateinit var INSTANCE: Preference

        fun getInstance(): Preference {
            if (!Companion::INSTANCE.isInitialized) {
                synchronized(this) {
                    if (!Companion::INSTANCE.isInitialized) {
                        INSTANCE = Preference()
                    }
                }
            }
            return INSTANCE
        }
    }

    var positionScreen: Int by IntPreferenceDelegate(
        PreferenceManager.preferences, POSITION_SCREEN, Constants.LANGUAGE_SCREEN_POSITION
    )

//    var restoreOption: String by StringPreferenceDelegate(
//        PreferenceManager.preferences, RESTORE_COUNT, SubscriptionType.WEEK.toString()
//    )

    var savedMode: Int by IntPreferenceDelegate(
        PreferenceManager.preferences,
        NIGHT_MODE,
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    )

    var language: String by StringPreferenceDelegate(
        PreferenceManager.preferences, LANGUAGE, ""
    )
    var isLoadedLanguageNative: Boolean by BooleanPreferenceDelegate(
        PreferenceManager.preferences, IS_LOADED_LANGUAGE_NATIVE, false
    )
    var isUseSystemLanguage: Boolean by BooleanPreferenceDelegate(
        PreferenceManager.preferences, IS_USE_SYSTEM_LANGUAGE, false
    )
    var defaultLanguage: String by StringPreferenceDelegate(
        PreferenceManager.preferences, DEFAULT_LANGUAGE, ""
    )
}