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
    val user_id: Int,
    val status: String,
    val total: Double?, // Total can also be null for an empty cart
    // This is now nullable to safely handle empty carts from the API
    @SerializedName("product_id")
    val product_id: List<CartProduct>? 
)

// Request body for creating a new cart
// The @SerializedName annotation ensures the JSON field is named correctly.
data class CreateCartRequest(
    @SerializedName("user_id")
    val user_id: Int
)

// Request body for updating the cart via PATCH
// It will send the complete, updated list of products.
data class UpdateCartProductsRequest(
    @SerializedName("product_id")
    val products: List<CartProduct>
)
