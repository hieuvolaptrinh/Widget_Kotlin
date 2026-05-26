package com.example.baseapp.di

import com.example.baseapp.BuildConfig
import com.example.baseapp.data.remote.api.APICoin
import com.example.baseapp.data.remote.api.APIWidget
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

//Cung cấp thành phần mạng sử dụng hilt

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private val logging = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(180, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiWidget(retrofit: Retrofit): APIWidget {
        return retrofit.create(APIWidget::class.java)
    }

    @Provides
    @Singleton
    fun provideApiCoin(retrofit: Retrofit): APICoin {
        return retrofit.create(APICoin::class.java)
    }


}