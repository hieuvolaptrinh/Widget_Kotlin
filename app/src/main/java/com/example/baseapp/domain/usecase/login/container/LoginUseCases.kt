package com.example.baseapp.domain.usecase.login.container

import com.example.baseapp.domain.usecase.login.GetAllLoginsUseCase
import com.example.baseapp.domain.usecase.login.GetLoginByNameUseCase

data class LoginUseCases(
    val getAll: GetAllLoginsUseCase,
    val getByName: GetLoginByNameUseCase
)