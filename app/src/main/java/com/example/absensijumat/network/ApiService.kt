package com.example.absensijumat.network

import com.example.absensijumat.response.ActivityResponse
import com.example.absensijumat.response.LatestActivityResponse
import com.example.absensijumat.response.LoginResponse
import com.example.absensijumat.response.ProfileResponse
import com.example.absensijumat.response.YasinResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

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
    @Headers("Accept: application/json")
    @POST("attendance")
    fun attendance(
        @Header("Authorization") token: String,
        @Part("schedule_id") scheduleId: RequestBody,
        @Part image: MultipartBody.Part,
        @Part("latitude") latitude: RequestBody,
        @Part("longtitude") longtitude: RequestBody
    ): Call<Void>

    @Multipart
    @Headers("Accept: application/json")
    @POST("attendance/permission")
    fun submitPermission(
        @Header("Authorization") token: String,
        @Part("schedule_id") scheduleId: RequestBody,
        @Part("status") status: RequestBody,
        @Part("reason") description: RequestBody,
        @Part image: MultipartBody.Part
    ): Call<Void>

    @GET("user/activity/latest")
    fun getLatestActivity(
        @Header("Authorization") token: String
    ): Call<LatestActivityResponse>

    @GET("user/activity/all")
    fun getAllActivity(
        @Header("Authorization") token: String
    ): Call<ActivityResponse>

    @Multipart
    @POST("user/update/profile")
    fun updatePhotoProfile(
        @Header("Authorization") token: String,
        @Part photo: MultipartBody.Part
    ): Call<UploadResponse>

    @GET
    fun getYasin(@Url url: String = "https://equran.id/api/v2/surat/36"): Call<YasinResponse>

    data class UploadResponse(
        val success: Boolean,
        val message: String
    )
}
