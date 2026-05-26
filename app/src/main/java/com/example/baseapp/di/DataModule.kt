package com.example.baseapp.di

import com.example.baseapp.data.remote.api.APICoin
import com.example.baseapp.data.remote.api.APIWidget
import com.example.baseapp.data.remote.datasource.CoinRemoteDataSource
import com.example.baseapp.data.remote.datasource.CoinRemoteDataSourceImpl
import com.example.baseapp.data.remote.datasource.WidgetRemoteDataSourceImpl
import com.example.baseapp.data.remote.datasource.WidgetRemoteDatasource
import com.example.baseapp.data.repository.CoinRepositoryImpl
import com.example.baseapp.data.repository.WidgetRepositoryImpl
import com.example.baseapp.domain.repository.CoinRepository
import com.example.baseapp.domain.repository.WidgetRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Cung cấp truy cập database

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    //    khai báo dể sử dụng hilt, vòng đời nó là singleton
    @Provides
    @Singleton
    fun provideWidgeRemoteDataSource(
        api: APIWidget
    ): WidgetRemoteDatasource {
        return WidgetRemoteDataSourceImpl(api)
    }

    @Provides
    @Singleton
    fun provideWidgetRepository(
        remote: WidgetRemoteDatasource
    ): WidgetRepository {
        return WidgetRepositoryImpl(remote)
    }

    @Provides
    @Singleton
    fun provideCoinRemoteDataSource(
        api: APICoin
    ): CoinRemoteDataSource {
        return CoinRemoteDataSourceImpl(api)
    }

    @Provides
    @Singleton
    fun provideCoinRepository(
        remote: CoinRemoteDataSource
    ): CoinRepository {
        return CoinRepositoryImpl(remote)
    }


}