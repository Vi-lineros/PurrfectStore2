package com.mycat.purrfectstore2.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("created_at")
    val createdAt: Long? = null,

    @SerializedName("name")
    var username: String,

    @SerializedName("email")
    var email: String,

    @SerializedName("role")
    var role: String,

    @SerializedName("first_name")
    var firstName: String? = null,

    @SerializedName("last_name")
    var lastName: String? = null,

    @SerializedName("shipping_address")
    var shippingAddress: String? = null,

    @SerializedName("phone")
    var phoneNumber: String? = null,

    @SerializedName("photo_url")
    var photoUrl: String? = null,

    @SerializedName("status")
    var status: String? = null,

    @SerializedName("cart")
    val cart: List<Cart>? 
)
