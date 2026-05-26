package com.example.baseapp.data.remote.api

import com.example.baseapp.data.remote.dto.Coin
import com.example.baseapp.data.remote.dto.IdentifyRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface APICoin {
    @GET("/api/coins/{id}/details")
    suspend fun getCoinById(
        @Path("id") id: String
    ): Response<Coin>

    @POST("/api/coins/search-image")
    suspend fun identifyCoin(@Body body: IdentifyRequest): Response<Coin>

    @GET("/api/coins/top20")
    suspend fun getTop20Coin(): Response<List<Coin>>
}