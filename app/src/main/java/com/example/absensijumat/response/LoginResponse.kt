package com.example.absensijumat.response

data class LoginResponse(
    val success: Boolean,
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