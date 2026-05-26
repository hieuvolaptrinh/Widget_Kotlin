package com.example.baseapp.data.remote.datasource

import com.example.baseapp.data.remote.Resource
import com.example.baseapp.data.remote.dto.WidgetPack

interface WidgetRemoteDatasource {

    suspend fun getThemePacksById(id: String): Resource<WidgetPack>
}