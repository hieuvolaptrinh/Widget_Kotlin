package com.example.baseapp.data.remote.datasource

import com.example.baseapp.data.remote.Resource
import com.example.baseapp.data.remote.dto.Coin
import com.example.baseapp.data.remote.dto.IdentifyRequest

interface CoinRemoteDataSource {

    suspend fun getCoinById(id: String): Resource<Coin>

    suspend fun identifyCoin(request: IdentifyRequest): Resource<Coin>

    suspend fun getTop20Coin(): Resource<List<Coin>>
}