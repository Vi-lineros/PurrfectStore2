package com.mycat.purrfectstore2.model

import com.google.gson.annotations.SerializedName
data class CreateProductResponse(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("price")
    val price: Double?,
    @SerializedName("stock")
    val stock: Int?,
    @SerializedName("image")
    val image: List<ProductImage>?
)
