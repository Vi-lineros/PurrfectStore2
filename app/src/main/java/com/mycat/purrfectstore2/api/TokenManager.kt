package com.mycat.purrfectstore2.api

import android.content.Context
import android.content.SharedPreferences

class TokenManager (context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    fun saveAuth(token: String, userName: String, userEmail: String) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_USER_NAME, userName)
            putString(KEY_USER_EMAIL, userEmail)
            commit()
        }
    }
    fun getToken(): String?{
        return prefs.getString(KEY_TOKEN, null)
    }
    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)
    fun isLoggedIn(): Boolean = getToken() != null
    fun clear(){
        prefs.edit().clear().commit()
    }
    companion object{
        private const val PREFS_NAME = "session"
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"

    }
}