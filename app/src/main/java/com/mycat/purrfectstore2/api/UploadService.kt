package com.mycat.purrfectstore2.api

import com.mycat.purrfectstore2.model.ProductImage
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
interface UploadService {
    @Multipart
    @POST("upload/image")
    suspend fun uploadImages(
        @Part image: MultipartBody.Part
    ): List<ProductImage>
}