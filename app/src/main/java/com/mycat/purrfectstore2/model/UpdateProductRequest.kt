package com.mycat.purrfectstore2.model

import com.google.gson.annotations.SerializedName

data class UpdateProductRequest(
    @SerializedName("name") val name: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("stock") val stock: Int? = null,
    @SerializedName("image") val image: List<ProductImage>? = null
) : java.io.Serializable
