package com.example.baseapp.data.remote.api

import com.example.baseapp.data.remote.ApiResponse
import com.example.baseapp.data.remote.dto.WidgetPack
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface APIWidget {

    //    69e4714f9708a69fde4102af => đây là id vì mình chỉ cần lấy mẫu thôi
    @GET("/v4/theme-packs/{id}")
    suspend fun getThemePacksById(
        @Path("id") id: String
    ): Response<ApiResponse<WidgetPack>>
}