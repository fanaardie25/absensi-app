package com.example.absensijumat.network

import com.example.absensijumat.response.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    fun login(@Body params: Map<String, String>): Call<LoginResponse>
}