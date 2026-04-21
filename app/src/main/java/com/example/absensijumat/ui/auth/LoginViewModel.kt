package com.example.absensijumat.ui.auth

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.absensijumat.network.RetrofitClient
import com.example.absensijumat.response.LoginResponse
import com.example.absensijumat.utils.ErrorHandler
import com.example.absensijumat.utils.SessionManager
import com.google.gson.Gson
import retrofit2.Callback


class LoginViewModel: ViewModel() {
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")

    fun clearError() {
        errorMessage = ""
    }

    fun LoginRequest(login: String, password: String, onContext: Context, onSuccess: (String) -> Unit) {
        if (login.isEmpty() || password.isEmpty()) {
            errorMessage = "NIS/Email atau password tidak boleh kosong"
            return
        }

        isLoading = true
        val params = mapOf("login" to login, "password" to password)

        RetrofitClient.instance.login(params).enqueue(object : Callback<LoginResponse> {

            override fun onResponse(
                call: retrofit2.Call<LoginResponse?>,
                response: retrofit2.Response<LoginResponse?>
            ) {
                isLoading = false

                if (response.isSuccessful) {
                    val body = response.body()
                    val token = body?.access_token

                    if (token != null) {
                        val session = SessionManager(onContext)
                        session.saveAuthToken(token)
                        errorMessage = ""
                        onSuccess(token)
                    } else {
                        errorMessage = "Login gagal: Token tidak ditemukan"
                    }
                } else {
                    val errorString = response.errorBody()?.string()
                    try {
                        val errorObj = Gson().fromJson(errorString, LoginResponse::class.java)

                        errorMessage = errorObj.message ?: ErrorHandler.getFriendlyMessage(response.code())
                    } catch (e: Exception) {
                        errorMessage = ErrorHandler.getFriendlyMessage(response.code())
                    }
                }
            }

            override fun onFailure(
                call: retrofit2.Call<LoginResponse?>,
                t: Throwable
            ) {
                isLoading = false
                errorMessage = ErrorHandler.getFriendlyMessage(t)
            }
        })
    }
}
