package com.mycat.purrfectstore2.model

import com.google.gson.annotations.SerializedName

data class CartProduct(
    val product_id: Int,
    var quantity: Int,
    var product_details: Product? = null 
)

data class Cart(
    val id: Int,
    @SerializedName("created_at")
    val created_at: Long,
    val user_id: Int,
    val status: String,
    val total: Double?,
    @SerializedName("product_id")
    val product_id: List<CartProduct>? 
)

data class CreateCartRequest(
    @SerializedName("user_id")
    val user_id: Int
)

data class UpdateCartProductsRequest(
    @SerializedName("product_id")
    val products: List<CartProduct>,
    val total: Double
)

data class UpdateCartStatusRequest(
    val status: String
)
