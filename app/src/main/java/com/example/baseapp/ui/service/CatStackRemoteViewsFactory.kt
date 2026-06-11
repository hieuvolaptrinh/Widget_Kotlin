package com.example.baseapp.ui.service

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.baseapp.R
import com.example.baseapp.ui.model.CatCard
import com.example.baseapp.ui.widget.WidgetCatStackProvider

// RemoteViewsService cấp item cho StackView/ListView/GridView trong widget.
class CatStackWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory? {
        return CatStackRemoteViewsFactory(context = applicationContext)
    }
}

class CatStackRemoteViewsFactory(private val context: Context) :
    RemoteViewsService.RemoteViewsFactory {

    private val items = mutableListOf<CatCard>()

    // Tạo view cho từng item, tương tự bind ViewHolder của RecyclerView.
    override fun getViewAt(position: Int): RemoteViews? {
        if (position !in items.indices) return null

        val item = items[position]
        val views = RemoteViews(context.packageName, R.layout.item_cat_stack)

        views.setImageViewResource(R.id.imgCatStack, item.imageRes)

        views.setTextViewText(R.id.tvTitleStack, item.title)
        views.setTextViewText(R.id.tvSubtitleStack, item.subtitle)

        val fillInIntent = Intent().apply {
            putExtra(WidgetCatStackProvider.EXTRA_CAT_POSITION, position)
            putExtra(WidgetCatStackProvider.EXTRA_CAT_TITLE, item.title)
        }

        // Provider tạo PendingIntent template, factory gắn dữ liệu riêng cho từng item.
        views.setOnClickFillInIntent(R.id.rootItem, fillInIntent)
        return views
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun onCreate() {
        loadItems()
    }

    override fun onDataSetChanged() {
        loadItems()
    }

    override fun onDestroy() {
        items.clear()
    }

    private fun loadItems() {
        items.clear()
        items.add(
            CatCard(
                title = "Mèo cute số 1",
                subtitle = "Vuốt để xem ảnh tiếp theo",
                imageRes = R.drawable.img_cat_1
            )
        )

        items.add(
            CatCard(
                title = "Mèo cute số 2",
                subtitle = "Ảnh con mèo bla bla bla",
                imageRes = R.drawable.img_cat_2
            )
        )

        items.add(
            CatCard(
                title = "Mèo cute số 3",
                subtitle = "Tap vào card để mở app",
                imageRes = R.drawable.img_cat_3
            )
        )
    }
}
