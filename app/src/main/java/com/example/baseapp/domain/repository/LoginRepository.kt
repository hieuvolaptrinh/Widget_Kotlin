package com.example.baseapp.domain.repository

import com.example.baseapp.data.local.entity.LoginPassword

interface LoginRepository {

    suspend fun getAll(): List<LoginPassword>

    suspend fun getByName(name: String): List<LoginPassword>

    suspend fun insert(item: LoginPassword)

    suspend fun insertAll(items: List<LoginPassword>)

    suspend fun update(item: LoginPassword)

    suspend fun updateAll(items: List<LoginPassword>)

    suspend fun delete(item: LoginPassword)
}