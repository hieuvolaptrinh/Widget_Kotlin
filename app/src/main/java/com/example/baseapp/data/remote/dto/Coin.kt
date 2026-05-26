package com.example.baseapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class Coin(
    @SerializedName("id")
    val id: String = "",
    val name: String = "",
    val imgCoin: List<String> = listOf(),
    val period: String = "",
    val country: String = "",
    val year: Int = 1999,
    val coinType: String = ""
)