package com.example.baseapp.data.remote

import com.example.baseapp.utils.LogUtils
import retrofit2.Response

const val TAG = "API Request"

/**
 * Dùng cho API trả về thường (không wrapper)
 * Response<Coin> → Resource<Coin>
 */
inline fun <T : Any> apiCall(request: () -> Response<T>): Resource<T> {
    return try {
        val result = request.invoke()

        if (result.isSuccessful) {
            val resultBody = result.body()
            LogUtils.d(TAG, "apiCall successful =${resultBody}")
            return Resource.Success(resultBody!!)
        } else {
            LogUtils.e(TAG, "apiCall result.code() =${result.code()}")
            Resource.Error(
                message = result.errorBody()?.string()
                    ?: "Unknown error",
                code = result.code()
            )
        }

    } catch (e: Exception) {
        Resource.Error(
            message = e.message ?: "Network error",
            code = null
        )
    }
}

/**
 * Dùng cho API trả về có wrapper ApiResponse<T>
 * Response<ApiResponse<WidgetPack>> → Resource<WidgetPack>
 * Tự động extract data từ wrapper
 */
inline fun <T : Any> apiCallWithWrapper(request: () -> Response<ApiResponse<T>>): Resource<T> {
    return try {
        val result = request.invoke()

        if (result.isSuccessful) {
            val apiResponse = result.body()
            LogUtils.d(TAG, "apiCallWithWrapper successful =${apiResponse}")
            // Extract data từ ApiResponse wrapper
            return Resource.Success(apiResponse?.data!!)
        } else {
            LogUtils.e(TAG, "apiCallWithWrapper result.code() =${result.code()}")
            Resource.Error(
                message = result.errorBody()?.string()
                    ?: "Unknown error",
                code = result.code()
            )
        }

    } catch (e: Exception) {
        Resource.Error(
            message = e.message ?: "Network error",
            code = null
        )
    }
}




