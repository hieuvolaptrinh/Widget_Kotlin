package com.example.baseapp.ui.widget


import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.os.Bundle
import android.widget.RemoteViews
import com.example.baseapp.R
import com.example.baseapp.extensions.dpToPx

class WidgetCatViewFlipperProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        val safeContext = context ?: return
        val safeAppWidgetManager = appWidgetManager ?: return

        appWidgetIds?.forEach { appWidgetId ->
            updateWidget(
                context = safeContext,
                appWidgetManager = safeAppWidgetManager,
                appWidgetId = appWidgetId
            )
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        val safeContext = context ?: return
        val safeAppWidgetManager = appWidgetManager ?: return
        updateWidget(safeContext, safeAppWidgetManager, appWidgetId)
    }

    companion object {
        private const val DEFAULT_WIDGET_SIZE_DP = 120
        private const val CORNER_RADIUS_DP = 28
        private const val BORDER_WIDTH_DP = 2
        private const val MAX_BITMAP_SIZE_PX = 512
        private val BORDER_COLOR = Color.parseColor("#00B1FF")

        private val imageViews = intArrayOf(
            R.id.imgCatFlipper1,
            R.id.imgCatFlipper2,
            R.id.imgCatFlipper3
        )
        private val images = intArrayOf(
            R.drawable.img_cat_1,
            R.drawable.img_cat_2,
            R.drawable.img_cat_3
        )

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_cat_view_flipper)
            val size = getWidgetSize(context, appWidgetManager, appWidgetId)

            imageViews.zip(images).forEach { (viewId, imageId) ->
                views.setImageViewBitmap(viewId, createRoundedBitmap(context, imageId, size))
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun getWidgetSize(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ): WidgetSize {
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val widthDp = options.getInt(
                AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,
                DEFAULT_WIDGET_SIZE_DP
            )
            val heightDp = options.getInt(
                AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,
                DEFAULT_WIDGET_SIZE_DP
            )

            return WidgetSize(
                width = widthDp.dpToPx(context).coerceAtMost(MAX_BITMAP_SIZE_PX),
                height = heightDp.dpToPx(context).coerceAtMost(MAX_BITMAP_SIZE_PX)
            )
        }

        private fun createRoundedBitmap(
            context: Context,
            imageResId: Int,
            size: WidgetSize
        ): Bitmap {
            val source = BitmapFactory.decodeResource(context.resources, imageResId)
            val output = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
            val scale = maxOf(
                size.width.toFloat() / source.width,
                size.height.toFloat() / source.height
            )
            val shader = BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            val matrix = Matrix().apply {
                setScale(scale, scale)
                postTranslate(
                    (size.width - source.width * scale) / 2f,
                    (size.height - source.height * scale) / 2f
                )
            }

            shader.setLocalMatrix(matrix)
            val radius = CORNER_RADIUS_DP.dpToPx(context).toFloat()
            val border = BORDER_WIDTH_DP.dpToPx(context).toFloat()
            val rect = RectF(0f, 0f, size.width.toFloat(), size.height.toFloat())
            val canvas = Canvas(output)

            canvas.drawRoundRect(rect, radius, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.shader = shader
            })

            rect.inset(border / 2f, border / 2f)
            canvas.drawRoundRect(rect, radius, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = BORDER_COLOR
                style = Paint.Style.STROKE
                strokeWidth = border
            })

            source.recycle()
            return output
        }

        private data class WidgetSize(val width: Int, val height: Int)
    }
}
