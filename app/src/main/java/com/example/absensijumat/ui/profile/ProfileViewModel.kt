package com.example.absensijumat.ui.profile

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.absensijumat.network.ApiService
import com.example.absensijumat.network.RetrofitClient
import com.example.absensijumat.response.ProfileResponse
import com.example.absensijumat.utils.SessionManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ProfileViewModel(): ViewModel() {
    var profileData by mutableStateOf<ProfileResponse?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")

    fun clearError() {
        errorMessage = ""
    }

    fun fetchProfile(context: Context){
        val sessionManager = SessionManager(context)
        val token = sessionManager.fetchAuthToken()
        if(token == null){
            errorMessage = "Token tidak ditemukan"
            return
        }
        isLoading = true
        errorMessage = ""
        val bearer = "Bearer $token"

        RetrofitClient.instance.getProfile(bearer).enqueue(object: Callback<ProfileResponse>{
            override fun onResponse(
                call: Call<ProfileResponse?>,
                response: Response<ProfileResponse?>
            ) {
               isLoading = false
                if (response.isSuccessful){
                    val body = response.body()
                    if(body != null){
                        profileData = body
                    }else{
                        errorMessage = "Data tidak ditemukan"
                    }
                }else{
                    errorMessage = "Gagal mengambil data: ${response.code()}"
                }
            }

            override fun onFailure(
                call: Call<ProfileResponse?>,
                t: Throwable?
            ) {
                isLoading = false
                errorMessage = "Terjadi Kesalahan: ${t?.message}"
            }
        })
    }

    fun logoutUser(context: Context, onLogoutSuccess: () -> Unit){
        val sessionManager = SessionManager(context)
        val token = sessionManager.fetchAuthToken()

        if (token == null){
            errorMessage = "Token tidak ditemukan"
            return
        }

        isLoading = true
        val bearer = "Bearer $token"

        RetrofitClient.instance.logout(bearer).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                isLoading = false
                sessionManager.deleteAuthToken()
                onLogoutSuccess()
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                isLoading = false
                sessionManager.deleteAuthToken()
                onLogoutSuccess()
            }
        })
    }


    fun uploadPhoto(context: Context, file: File) {
        val sessionManager = SessionManager(context)
        val token = sessionManager.fetchAuthToken()

        if (token == null) {
            errorMessage = "Token tidak ditemukan"
            return
        }
        isLoading = true
        val bearer = "Bearer $token"

        val requestFile = file.asRequestBody("image/webp".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("photo", file.name, requestFile)

        RetrofitClient.instance.updatePhotoProfile(bearer, body).enqueue(object : Callback<ApiService.UploadResponse> {
            override fun onResponse(call: Call<ApiService.UploadResponse>, response: Response<ApiService.UploadResponse>) {
                isLoading = false
                if (response.isSuccessful) {
                    fetchProfile(context)
                } else {
                    errorMessage = "Gagal upload: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<ApiService.UploadResponse>, t: Throwable) {
                isLoading = false
                errorMessage = "Terjadi Kesalahan: ${t.message}"
            }
        })
    }
}