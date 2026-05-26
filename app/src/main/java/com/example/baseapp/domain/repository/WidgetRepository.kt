package com.example.baseapp.domain.repository

import com.example.baseapp.data.remote.Resource
import com.example.baseapp.data.remote.dto.WidgetPack

interface WidgetRepository {

    suspend fun getThemePacksById(id: String): Resource<WidgetPack>
}