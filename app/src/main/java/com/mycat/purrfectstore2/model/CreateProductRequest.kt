package com.mycat.purrfectstore2.model

import com.google.gson.annotations.SerializedName
data class CreateProductRequest(
    val name: String,
    val description: String?,
    val price: Double?,
    val stock: Int?,
    @SerializedName("image")
    val image: List<ProductImage>
)