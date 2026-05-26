package com.example.baseapp.di

import com.example.baseapp.domain.usecase.login.GetAllLoginsUseCase
import com.example.baseapp.domain.usecase.login.GetLoginByNameUseCase
import com.example.baseapp.domain.usecase.login.container.LoginUseCases
import com.example.baseapp.domain.usecase.widget.GetWidgetPackByIdUseCase
import com.example.baseapp.domain.usecase.widget.container.WidgetUseCases
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideWidgetPackUseCases(
        getWidgetPackByIdUseCase: GetWidgetPackByIdUseCase
    ): WidgetUseCases {
        return WidgetUseCases(
            getWidgetPackByIdUseCase
        )
    }


    @Provides
    fun provideLoginUseCases(
        getAll: GetAllLoginsUseCase,
        getByName: GetLoginByNameUseCase
    ): LoginUseCases {
        return LoginUseCases(
            getAll,
            getByName
        )
    }
}