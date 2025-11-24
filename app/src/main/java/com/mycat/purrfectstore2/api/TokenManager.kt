package com.mycat.purrfectstore2.api

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveAuthWithRole(token: String, userId: Int, userName: String, userEmail: String, userRole: String) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putInt(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, userName)
            putString(KEY_USER_EMAIL, userEmail)
            putString(KEY_USER_ROLE, userRole)
            apply()
        }
    }

    fun saveCartId(cartId: Int) {
        prefs.edit().apply {
            putInt(KEY_CART_ID, cartId)
            apply()
        }
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)

    fun getCartId(): Int = prefs.getInt(KEY_CART_ID, -1)

    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)

    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    fun getUserRole(): String? = prefs.getString(KEY_USER_ROLE, null)

    fun isLoggedIn(): Boolean = getToken() != null

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "session"
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_CART_ID = "cart_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ROLE = "user_role"
    }
}
