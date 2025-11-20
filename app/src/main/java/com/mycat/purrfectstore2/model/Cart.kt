package com.mycat.purrfectstore2.model

import com.google.gson.annotations.SerializedName

// Represents a product within the cart's JSON field
// Contains the product ID and its quantity.
data class CartProduct(
    val product_id: Int,
    var quantity: Int,
    // This field will be populated by the app after fetching product details separately
    var product_details: Product? = null 
)

// Represents the main cart object from your 'cart' table
data class Cart(
    val id: Int,
    @SerializedName("created_at")
    val created_at: Long, // Made this non-nullable for date calculations
    val user_id: Int,
    val status: String,
    val total: Double?,
    @SerializedName("product_id")
    val product_id: List<CartProduct>? 
)

// Request body for creating a new cart
data class CreateCartRequest(
    @SerializedName("user_id")
    val user_id: Int
)

// Request body for updating the cart via PATCH. Now includes the total.
data class UpdateCartProductsRequest(
    @SerializedName("product_id")
    val products: List<CartProduct>,
    val total: Double
)

// Request body for updating just the cart's status
data class UpdateCartStatusRequest(
    val status: String
)
