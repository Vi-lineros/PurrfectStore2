package com.mycat.purrfectstore2.api

import com.mycat.purrfectstore2.model.*
import retrofit2.http.*

interface CartService {

    // Creates a new cart for a given user ID
    @POST("cart")
    suspend fun createCart(@Body createCartRequest: CreateCartRequest): Cart

    // Gets the full cart object using the cart's own ID
    @GET("cart/{cart_id}")
    suspend fun getCart(@Path("cart_id") cartId: Int): Cart

    // Updates the list of products in the cart using its ID
    @PATCH("cart/{cart_id}")
    suspend fun updateCart(
        @Path("cart_id") cartId: Int,
        @Body updateRequest: UpdateCartProductsRequest
    ): Cart
    
    @PATCH("cart/{id}")
    suspend fun updateCartStatus(@Path("id") cartId: Int, @Body statusRequest: UpdateCartStatusRequest): Cart

    // Deletes an entire cart. This might be useful for other flows.
    @DELETE("cart/{cart_id}")
    suspend fun deleteCart(@Path("cart_id") cartId: Int)
}
