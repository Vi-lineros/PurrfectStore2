package com.mycat.purrfectstore2.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("authToken")
    val authToken: String
)
