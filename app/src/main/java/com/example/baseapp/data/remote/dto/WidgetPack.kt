package com.example.baseapp.data.remote.dto

import com.google.gson.annotations.SerializedName


// Main Model
data class WidgetPack(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val preview: String,
    val subPreview: String,
    val subPreviewType: String,
    val background: String,
    val categoryIds: List<Category>,
    val tag: String,
    val isPaid: Boolean,
    val order: Int,
    val widgets: List<Widget>,
    val icons: List<Icon>,
    val createdAt: String,
    val updatedAt: String
)

// Category Model
data class Category(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val iconUrl: String
)

// Widget Model
data class Widget(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val previewUrl: String,
    val imageUrl: List<ImageUrl>,
    val size: String,
    val type: String,
    val font: String,
    val textColor: String,
    val contentPosition: String
)

// Image Url Model
data class ImageUrl(
    val url: String,
    val backgroundUrl: String,
    val avatarUrl: String,
    val defaultAvatar: String,
    val position: String,
    val rotation: Int,
    val width: Int,
    val height: Int,
    val centerX: Int,
    val centerY: Int,
    @SerializedName("_id")
    val id: String
)

// Icon Model
data class Icon(
    @SerializedName("_id")
    val id: String,
    val appname: String,
    val imageUrl: String,
    val iosAppUrl: String,
    val androidAppUrl: String
)
