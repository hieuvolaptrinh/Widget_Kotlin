package com.example.baseapp.utils

import android.util.Log
import com.example.baseapp.BuildConfig
import com.example.baseapp.MyApplication
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.io.use
import kotlin.text.toByteArray

object LogUtils {
    private const val DEFAULT_TAG = "APP_DEBUG"

    fun d(tag: String = DEFAULT_TAG, message: String) {
        if (BuildConfig.DEBUG) Log.d(tag, message)
    }

    fun e(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) Log.e(tag, message, throwable)
    }

    fun i(tag: String = DEFAULT_TAG, message: String) {
        if (BuildConfig.DEBUG) Log.i(tag, message)
    }

    fun w(tag: String = DEFAULT_TAG, message: String) {
        if (BuildConfig.DEBUG) Log.w(tag, message)
    }

    fun writeToFile(tag: String, message: String) {
        if (!BuildConfig.DEBUG) return

        try {
            val context = MyApplication.getInstance().applicationContext

            // ✅ Ghi vào thư mục app-specific ngoài (an toàn, không hiện Gallery)
            val logDir = File(context.getExternalFilesDir(null), "logs")
            if (!logDir.exists()) logDir.mkdirs()

            val logFile = File(logDir, "Logo normal")

            val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val fullMessage = "[$time][$tag] $message\n"

            FileOutputStream(logFile, true).use { fos ->
                fos.write(fullMessage.toByteArray())
            }

        } catch (e: Exception) {
            Log.e("LMLogUtils", "Error writing log: ${e.message}")
        }
    }
}