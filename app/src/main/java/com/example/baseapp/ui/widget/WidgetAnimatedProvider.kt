package com.example.baseapp.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Rect
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.widget.RemoteViews
import com.example.baseapp.Constants
import com.example.baseapp.R
import com.example.baseapp.extensions.dpToPx
import com.example.baseapp.ui.service.WidgetUpdateService
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WidgetAnimatedProvider : AppWidgetProvider() {

    override fun onUpdate(
            context: Context?,
            appWidgetManager: AppWidgetManager?,
            appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        context?.applicationContext?.let { startScheduler(it) }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        if (intent?.action == Constants.ACTION_PINNED && context != null) {
            val pendingResult = goAsync()
            val appContext = context.applicationContext

            startWidgetService(appContext)
            downloadImage(appContext) {
                startScheduler(appContext)
                pendingResult.finish()
            }
        }
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        context?.applicationContext?.let {
            startWidgetService(it)
            downloadImage(it) { startScheduler(it) }
        }
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        stopScheduler()
    }

    companion object {
        private const val IMAGE_URL =
                "https://candy-storage.s3.ap-southeast-1.amazonaws.com/themes/uploads/1c846501-5b64-4a37-ae25-32c76ad36150.png"
        private const val IMAGE_FILE_NAME = "animated_widget.webp"
        private const val DEFAULT_WIDGET_SIZE_DP = 200
        private const val FRAME_DELAY_MS = 120L

        private var handler: Handler? = null
        private var frameRunnable: Runnable? = null
        private var animatedDrawable: AnimatedImageDrawable? = null

        fun startScheduler(context: Context) {
            val appContext = context.applicationContext
            val file = File(appContext.filesDir, IMAGE_FILE_NAME)

            if (getWidgetIds(appContext).isEmpty()) {
                stopScheduler()
                return
            }

            if (!file.exists()) {
                updateWidgets(appContext, null)
                downloadImage(appContext) { startScheduler(appContext) }
                return
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                updateWidgets(appContext, null)
                return
            }

            stopScheduler()
            handler = Handler(Looper.getMainLooper())

            val drawable = decodeAnimatedDrawable(file)
            if (drawable == null) {
                updateWidgets(appContext, null)
                return
            }

            animatedDrawable = drawable
            drawable.callback = drawableCallback()
            drawable.repeatCount = AnimatedImageDrawable.REPEAT_INFINITE
            drawable.start()

            frameRunnable =
                    object : Runnable {
                        override fun run() {
                            updateWidgets(appContext, drawable)
                            handler?.postDelayed(this, FRAME_DELAY_MS)
                        }
                    }
            handler?.post(frameRunnable!!)
        }

        fun stopScheduler() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                animatedDrawable?.stop()
            }
            frameRunnable?.let { handler?.removeCallbacks(it) }
            animatedDrawable = null
            frameRunnable = null
            handler = null
        }

        private fun updateWidgets(context: Context, animatedFrame: Drawable?) {
            val widgetIds = getWidgetIds(context)
            if (widgetIds.isEmpty()) {
                stopScheduler()
                return
            }

            val appWidgetManager = AppWidgetManager.getInstance(context)
            widgetIds.forEach { widgetId ->
                val views = RemoteViews(context.packageName, R.layout.widget_animated_png)
                val bitmap =
                        animatedFrame?.let {
                            drawToWidgetBitmap(context, appWidgetManager, widgetId, it)
                        } ?: decodeStaticBitmap(context, appWidgetManager, widgetId)

                if (bitmap != null) {
                    views.setImageViewBitmap(R.id.imgAnimated, bitmap)
                } else {
                    views.setImageViewResource(R.id.imgAnimated, R.drawable.img_anime_girl)
                }
                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }

        private fun downloadImage(context: Context, onFinished: (() -> Unit)? = null) {
            CoroutineScope(Dispatchers.IO).launch {
                var connection: HttpURLConnection? = null
                try {
                    connection = URL(IMAGE_URL).openConnection() as HttpURLConnection
                    connection.connectTimeout = 10_000
                    connection.readTimeout = 15_000

                    if (connection.responseCode in 200..299) {
                        val file = File(context.filesDir, IMAGE_FILE_NAME)
                        connection.inputStream.use { input ->
                            file.outputStream().use { output -> input.copyTo(output) }
                        }
                    } else {
                        Log.e("WidgetAnimated", "Download failed: HTTP ${connection.responseCode}")
                    }
                } catch (e: Exception) {
                    Log.e("WidgetAnimated", "Error downloading image", e)
                } finally {
                    connection?.disconnect()
                    withContext(Dispatchers.Main) { onFinished?.invoke() }
                }
            }
        }

        private fun decodeAnimatedDrawable(file: File): AnimatedImageDrawable? {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return null

            return try {
                ImageDecoder.decodeDrawable(ImageDecoder.createSource(file)) as? AnimatedImageDrawable
            } catch (e: Exception) {
                Log.e("WidgetAnimated", "Error decoding animated image", e)
                null
            }
        }

        private fun decodeStaticBitmap(
                context: Context,
                appWidgetManager: AppWidgetManager,
                appWidgetId: Int
        ): Bitmap? {
            val file = File(context.filesDir, IMAGE_FILE_NAME)
            if (!file.exists()) return null

            val size = getWidgetSize(context, appWidgetManager, appWidgetId)
            val bitmap =
                    BitmapFactory.Options()
                            .apply { inJustDecodeBounds = true }
                            .also { BitmapFactory.decodeFile(file.absolutePath, it) }
                            .run {
                                inJustDecodeBounds = false
                                inSampleSize = calculateInSampleSize(outWidth, outHeight, size)
                                BitmapFactory.decodeFile(file.absolutePath, this)
                            }
                            ?: return null

            val rendered =
                    drawToWidgetBitmap(
                            context,
                            appWidgetManager,
                            appWidgetId,
                            BitmapDrawable(context.resources, bitmap)
                    )
            bitmap.recycle()
            return rendered
        }

        private fun drawToWidgetBitmap(
                context: Context,
                appWidgetManager: AppWidgetManager,
                appWidgetId: Int,
                drawable: Drawable
        ): Bitmap {
            val size = getWidgetSize(context, appWidgetManager, appWidgetId)
            val bitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
            val drawableWidth = drawable.intrinsicWidth.takeIf { it > 0 } ?: size.width
            val drawableHeight = drawable.intrinsicHeight.takeIf { it > 0 } ?: size.height
            val scale =
                    minOf(
                            size.width.toFloat() / drawableWidth,
                            size.height.toFloat() / drawableHeight
                    )
            val width = (drawableWidth * scale).toInt()
            val height = (drawableHeight * scale).toInt()
            val left = (size.width - width) / 2
            val top = (size.height - height) / 2

            drawable.bounds = Rect(left, top, left + width, top + height)
            drawable.draw(Canvas(bitmap))
            return bitmap
        }

        private fun drawableCallback() =
                object : Drawable.Callback {
                    override fun invalidateDrawable(who: Drawable) = Unit

                    override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
                        handler?.postAtTime(what, `when`)
                    }

                    override fun unscheduleDrawable(who: Drawable, what: Runnable) {
                        handler?.removeCallbacks(what)
                    }
                }

        private fun getWidgetIds(context: Context): IntArray =
                AppWidgetManager.getInstance(context)
                        .getAppWidgetIds(ComponentName(context, WidgetAnimatedProvider::class.java))

        private fun getWidgetSize(
                context: Context,
                appWidgetManager: AppWidgetManager,
                appWidgetId: Int
        ): WidgetSize {
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val widthDp =
                    options.getInt(
                            AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,
                            DEFAULT_WIDGET_SIZE_DP
                    )
            val heightDp =
                    options.getInt(
                            AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,
                            DEFAULT_WIDGET_SIZE_DP
                    )

            return WidgetSize(widthDp.dpToPx(context), heightDp.dpToPx(context))
        }

        private fun calculateInSampleSize(width: Int, height: Int, size: WidgetSize): Int {
            var sampleSize = 1
            val halfWidth = width / 2
            val halfHeight = height / 2

            while (halfWidth / sampleSize >= size.width &&
                    halfHeight / sampleSize >= size.height
            ) {
                sampleSize *= 2
            }
            return sampleSize
        }

        private fun startWidgetService(context: Context) {
            val serviceIntent = Intent(context, WidgetUpdateService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }

        private data class WidgetSize(val width: Int, val height: Int)
    }
}
