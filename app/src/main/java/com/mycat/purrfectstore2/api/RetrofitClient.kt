package com.mycat.purrfectstore2.api

import android.content.Context
import com.mycat.purrfectstore2.api.ApiConfig.authBaseUrl
import com.mycat.purrfectstore2.api.ApiConfig.storeBaseUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private fun baseOkHttpBuilder(): OkHttpClient.Builder{
        val loggin = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggin)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
    }
    private fun retrofit(baseUrl: String, client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    fun createAuthService(context: Context, requiresAuth: Boolean = false): AuthService {
        var clientBuilder = baseOkHttpBuilder()
        if (requiresAuth) {
            val tokenManager = TokenManager(context)
            clientBuilder.addInterceptor(AuthInterceptor{tokenManager.getToken()})
        }
        val client = clientBuilder.build()
        return retrofit(authBaseUrl, client).create(AuthService::class.java)
        }
    fun createProductService(context: Context): ProductService {
        val tokenManager = TokenManager(context)
        val client = baseOkHttpBuilder()
            .addInterceptor(AuthInterceptor{tokenManager.getToken()})
            .build()
        return retrofit(storeBaseUrl, client).create(ProductService::class.java)
    }
    fun createUploadService(context: Context): UploadService {
        val tokenManager = TokenManager(context)
        val client = baseOkHttpBuilder()
            .addInterceptor(AuthInterceptor{tokenManager.getToken()})
            .build()
        return retrofit(storeBaseUrl, client).create(UploadService::class.java)
    }
}