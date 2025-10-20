package com.mycat.purrfectstore2.model
import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("price") val price: Double,
    @SerializedName("stock")val stock: Int,
    @SerializedName("image")val images: List<ProductImage> = emptyList()
) : java.io.Serializable