package com.example.baseapp.data.remote

data class ApiResponse<T>(
    val error: Int,
    val statusCode: Int,
    val message: String,
    val data: T
)