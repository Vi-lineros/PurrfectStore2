package com.mycat.purrfectstore2.api

import com.mycat.purrfectstore2.model.AuthResponse
import com.mycat.purrfectstore2.model.LoginRequest
import com.mycat.purrfectstore2.model.User
import com.mycat.purrfectstore2.model.SignupBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse
    @GET("auth/me")
    suspend fun getMe(): User

    @POST ("auth/signup")
    suspend fun signUp(@Body request: SignupBody): AuthResponse
}
