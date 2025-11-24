package com.mycat.purrfectstore2.api

import com.mycat.purrfectstore2.model.*
import retrofit2.http.*

interface CartService {

    @POST("cart")
    suspend fun createCart(@Body createCartRequest: CreateCartRequest): Cart

    @GET("cart/{cart_id}")
    suspend fun getCart(@Path("cart_id") cartId: Int): Cart

    @GET("cart")
    suspend fun getCarritos(): List<Cart>

    @PATCH("cart/{cart_id}")
    suspend fun updateCart(
        @Path("cart_id") cartId: Int,
        @Body updateRequest: UpdateCartProductsRequest
    ): Cart
    
    @PATCH("cart/{id}")
    suspend fun updateCartStatus(@Path("id") cartId: Int, @Body statusRequest: UpdateCartStatusRequest): Cart

}
