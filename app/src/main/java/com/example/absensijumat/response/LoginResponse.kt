package com.example.absensijumat.response

import android.os.Message

data class LoginResponse(
    val success: Boolean,
    val message: String?,
    val data: UserData?,
    val access_token: String?
)

data class UserData(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val class_id: Int?
)