package com.mycat.purrfectstore2.model

import com.google.gson.annotations.SerializedName

// The JSON response from the server uses "authToken" as the key.
// This class now correctly maps that key to the authToken variable.
data class AuthResponse(
    @SerializedName("authToken") // Corrected to match the server response.
    val authToken: String
)
