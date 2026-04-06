package com.example.absensijumat.ui.auth

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.absensijumat.network.RetrofitClient
import com.example.absensijumat.utils.SessionManager
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")
    var successMessage by mutableStateOf("")

    fun changePassword(
        context: Context,
        oldPassword: String,
        newPassword: String,
        confirmPassword: String,
        onSuccess: () -> Unit
    ) {
        if (newPassword != confirmPassword) {
            errorMessage = "Konfirmasi password tidak cocok"
            return
        }

        val sessionManager = SessionManager(context)
        val token = sessionManager.fetchAuthToken() ?: return

        isLoading = true
        val bearer = "Bearer $token"
        val params = mapOf(
            "old_password" to oldPassword,
            "new_password" to newPassword,
            "new_password_confirmation" to confirmPassword
        )

        RetrofitClient.instance.changePassword(bearer, params).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                isLoading = false
                if (response.isSuccessful) {
                    successMessage = "Password berhasil diubah"
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    errorMessage = try {
                        val jsonObject = JSONObject(errorBody.toString())
                        jsonObject.getString("message")
                    } catch (e: Exception) {
                        "Gagal mengubah password: ${response.code()}"
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                isLoading = false
                errorMessage = "Terjadi kesalahan: ${t.message}"
            }
        })
    }

    fun clearError() {
        errorMessage = ""
    }

    fun clearSuccess() {
        successMessage = ""
    }
}
