package com.mycat.purrfectstore2.api

import com.google.gson.annotations.SerializedName
import com.mycat.purrfectstore2.model.User
import retrofit2.http.*

data class UserCreationRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("role")
    val role: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("shipping_address")
    val shippingAddress: String,
    @SerializedName("phone")
    val phoneNumber: String
)

interface UserService {

    @GET("user")
    suspend fun getUsers(): List<User>

    @GET("user/{user_id}")
    suspend fun getUserById(@Path("user_id") userId: Int): User

    @POST("user")
    suspend fun createUser(@Body user: UserCreationRequest): User

    @PATCH("user/{user_id}")
    suspend fun updateUser(@Path("user_id") userId: Int, @Body user: Map<String, @JvmSuppressWildcards Any>): User

    @DELETE("user/{user_id}")
    suspend fun deleteUser(@Path("user_id") userId: Int)
}
