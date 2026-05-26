package com.example.baseapp.di

import android.content.Context
import androidx.room.Room
import com.example.baseapp.data.local.dao.LoginPasswordDao
import com.example.baseapp.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

//Cung cấp layer data

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideLoginDao(
        database: AppDatabase
    ): LoginPasswordDao {
        return database.loginDao()
    }
}