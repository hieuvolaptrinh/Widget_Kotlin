package com.example.baseapp.data.repository

import com.example.baseapp.data.remote.datasource.WidgetRemoteDatasource
import com.example.baseapp.domain.repository.WidgetRepository

class WidgetRepositoryImpl(private val remote: WidgetRemoteDatasource) : WidgetRepository {
    override suspend fun getThemePacksById(id: String) =
        remote.getThemePacksById(id);

}