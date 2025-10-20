package com.mycat.purrfectstore2.model

import com.google.gson.annotations.SerializedName

data class SignupBody(
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)