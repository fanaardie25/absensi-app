package com.example.absensijumat.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SessionManager(context: Context) {
    private var prefs: SharedPreferences  = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

    companion object{
        const val USER_TOKEN = "user_token"
        const val CLASS_ID = "class_id"
    }

    @SuppressLint("CommitPrefEdits")
    fun saveAuthToken(token: String) {
        prefs.edit {
            putString(USER_TOKEN, token)
        }
    }

    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun saveClassId(classId: Int){
        prefs.edit {
            putInt(CLASS_ID, classId)
        }
    }

    fun fetchClassId(): Int? {
        return prefs.getInt(CLASS_ID, 0)
    }
}