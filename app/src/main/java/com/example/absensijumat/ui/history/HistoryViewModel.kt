package com.example.absensijumat.ui.history

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.absensijumat.network.RetrofitClient
import com.example.absensijumat.response.ActivityResponse
import com.example.absensijumat.response.AttendanceData
import com.example.absensijumat.response.AttendanceDataAll
import com.example.absensijumat.response.LatestActivityResponse
import com.example.absensijumat.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HistoryViewModel(): ViewModel() {

    var activityList by mutableStateOf<List<AttendanceDataAll>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")   
    
    fun getAllActivity(context: Context){
        val sessionManager = SessionManager(context)
        val token = sessionManager.fetchAuthToken()

        if (token == null){
            errorMessage = "Token tidak ditemukan"
            return
        }
        
        isLoading = true
        errorMessage = ""
        val bearer = "Bearer $token"

        RetrofitClient.instance.getAllActivity(bearer).enqueue(object: Callback<ActivityResponse> {
            override fun onResponse(
                call: Call<ActivityResponse>,
                response: Response<ActivityResponse>
            ) {
                isLoading = false
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        activityList = body.data ?: emptyList()
                    } else {
                        errorMessage = "Data tidak ditemukan"
                    }
                } else {
                    errorMessage = "Gagal mengambil data: ${response.code()}"
                }
            }

            override fun onFailure(
                call: Call<ActivityResponse>,
                t: Throwable
            ) {
                isLoading = false
                errorMessage = "Terjadi Kesalahan: ${t.message}"
            }
        })
    }
}
