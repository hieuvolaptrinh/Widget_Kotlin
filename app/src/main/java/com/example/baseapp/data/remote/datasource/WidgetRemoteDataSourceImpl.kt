package com.example.baseapp.data.remote.datasource

import com.example.baseapp.data.remote.api.APIWidget

import com.example.baseapp.data.remote.apiCallWithWrapper

class WidgetRemoteDataSourceImpl(private val api: APIWidget) : WidgetRemoteDatasource {
    override suspend fun getThemePacksById(id: String) =
        apiCallWithWrapper {
            api.getThemePacksById(id)
        }

}