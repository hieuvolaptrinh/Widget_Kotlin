package com.example.baseapp.data.remote.datasource

import com.example.baseapp.data.remote.ApiResponse
import com.example.baseapp.data.remote.Resource
import com.example.baseapp.data.remote.dto.WidgetPack

interface WidgetRemoteDatasource {

//    apiCallWithWrapper đã extract WigetPackModel ra khỏi ApiResponse rồi nên ở đây mình chỉ cần trả về WidgetPack thôi
    suspend fun getThemePacksById(id: String): Resource<WidgetPack>
}