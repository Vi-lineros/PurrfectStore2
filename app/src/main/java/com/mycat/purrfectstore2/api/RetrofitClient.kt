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

    // The token can now be passed directly, to be used immediately after login,
    // bypassing the need to read it from SharedPreferences.
    fun createAuthService(context: Context, requiresAuth: Boolean = false, token: String? = null): AuthService {
        val clientBuilder = baseOkHttpBuilder()
        if (requiresAuth) {
            // Rewriting the logic to be more explicit and avoid the type mismatch error.
            val tokenProvider: () -> String?
            if (token != null) {
                tokenProvider = { token } // Use the token passed as an argument
            } else {
                tokenProvider = { TokenManager(context).getToken() } // Fallback to getting token from storage
            }
            clientBuilder.addInterceptor(AuthInterceptor(tokenProvider))
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

    fun createUserService(context: Context): UserService {
        val tokenManager = TokenManager(context)
        val client = baseOkHttpBuilder()
            .addInterceptor(AuthInterceptor{tokenManager.getToken()})
            .build()
        return retrofit(storeBaseUrl, client).create(UserService::class.java)
    }

    fun createUploadService(context: Context): UploadService {
        val tokenManager = TokenManager(context)
        val client = baseOkHttpBuilder()
            .addInterceptor(AuthInterceptor{tokenManager.getToken()})
            .build()
        return retrofit(storeBaseUrl, client).create(UploadService::class.java)
    }
}