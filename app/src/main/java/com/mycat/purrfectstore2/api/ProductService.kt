package com.mycat.purrfectstore2.api

import com.mycat.purrfectstore2.model.CreateProductRequest
import com.mycat.purrfectstore2.model.CreateProductResponse
import com.mycat.purrfectstore2.model.Product
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
interface ProductService {
    @GET("product")
    suspend fun getProducts(): List<Product>
    @POST("product")
    suspend fun createProduct(@Body request: CreateProductRequest): CreateProductResponse
    @GET("product/{id}")
    suspend fun getProductId(@Path("id") id: Int): Product

}