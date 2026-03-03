package com.example.absensijumat.network

import com.example.absensijumat.response.LoginResponse
import com.example.absensijumat.response.ProfileResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @POST("auth/login")
    fun login(@Body params: Map<String, String>): Call<LoginResponse>

    @GET("me")
    fun getProfile(
        @Header("Authorization") token: String
    ): Call<ProfileResponse>

    @POST("auth/logout")
    fun logout(
        @Header("Authorization") token: String
    ): Call<Void>

    @Multipart
    @POST("attendance")
    fun attendance(
        @Header("Authorization") token: String,
        @Part("schedule_id") scheduleId: RequestBody,
        @Part image: MultipartBody.Part,
        @Part("latitude") latitude: RequestBody,
        @Part("longtitude") longtitude: RequestBody
    ): Call<Void>

}
