package com.example.baseapp.data.repository

import com.example.baseapp.data.remote.datasource.CoinRemoteDataSource
import com.example.baseapp.data.remote.dto.IdentifyRequest
import com.example.baseapp.domain.repository.CoinRepository

class CoinRepositoryImpl(
    private val remote: CoinRemoteDataSource
) : CoinRepository {

    override suspend fun getCoinById(id: String) =
        remote.getCoinById(id)

    override suspend fun identifyCoin(request: IdentifyRequest) =
        remote.identifyCoin(request)

    override suspend fun getTop20Coin() =
        remote.getTop20Coin()
}