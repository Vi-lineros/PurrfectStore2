package com.mycat.purrfectstore2.api

import okhttp3.Response
import okhttp3.Interceptor
class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokenProvider()
        val request = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .addHeader("Authorization","Bearer $token")
                .build()
        } else {
            original
        }
        return chain.proceed(request)
    }
}