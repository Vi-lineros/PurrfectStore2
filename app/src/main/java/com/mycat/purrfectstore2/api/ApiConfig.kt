package com.mycat.purrfectstore2.api

    import com.mycat.purrfectstore2.BuildConfig
object ApiConfig {
    val storeBaseUrl: String = BuildConfig.XANO_STORE_BASE
    val authBaseUrl: String = BuildConfig.XANO_AUTH_BASE
    val tokenItlSec: Int = BuildConfig.XANO_TOKEN_TTL_SEC
}