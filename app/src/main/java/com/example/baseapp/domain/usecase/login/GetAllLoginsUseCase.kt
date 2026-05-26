package com.example.baseapp.domain.usecase.login

import com.example.baseapp.data.local.entity.LoginPassword
import com.example.baseapp.domain.repository.LoginRepository
import javax.inject.Inject

class GetAllLoginsUseCase @Inject constructor(
    private val repository: LoginRepository
) {
    suspend operator fun invoke(): List<LoginPassword> {
        return repository.getAll()
    }
}