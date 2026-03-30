package com.example.absensijumat.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.absensijumat.network.RetrofitClient
import com.example.absensijumat.response.AttendanceData
import com.example.absensijumat.response.LatestActivityResponse
import com.example.absensijumat.response.ProfileResponse
import com.example.absensijumat.utils.SessionManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class HomeViewModel(): ViewModel() {
    var userData by mutableStateOf<ProfileResponse?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")

    var activityData by mutableStateOf<AttendanceData?>(null)

    fun clearError() {
        errorMessage = ""
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(context: Context, onSuccess: (Double, Double) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val priority = Priority.PRIORITY_HIGH_ACCURACY

        fusedLocationClient.getCurrentLocation(priority, null)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    onSuccess(location.latitude, location.longitude)
                } else {
                    errorMessage = "GPS sedang mencari sinyal. Coba beberapa saat lagi."
                }
            }.addOnFailureListener {
                errorMessage = "Kesalahan lokasi: ${it.message}"
            }
    }

    fun submitAttendance(
        context: Context,
        scheduleId: Int,
        bitmap: Bitmap,
        latitude: Double,
        longtitude: Double,
        onAttendanceSuccess: () -> Unit
    ) {
        val sessionManager = SessionManager(context)
        val token = sessionManager.fetchAuthToken() ?: return

        isLoading = true
        val bearer = "Bearer $token"

        val file = File(context.cacheDir, "attendance_image.jpg")
        val os = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, os)
        os.flush()
        os.close()

        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("photo", file.name, requestFile)

        val idBody = scheduleId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val latBody = latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val lonBody = longtitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        RetrofitClient.instance.attendance(bearer, idBody, imagePart, latBody, lonBody)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    isLoading = false
                    if (response.isSuccessful)
                    {
                        onAttendanceSuccess()
                    } else {
                        val errorJsonString = response.errorBody()?.string()
                        val errorMessageFromServer = try {
                            val jsonObject = JSONObject(errorJsonString)
                            jsonObject.getString("message")
                        } catch (e: Exception) {
                            "Gagal: ${response.code()}"
                        }
                        errorMessage = "Gagal: $errorMessageFromServer"
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    isLoading = false
                    errorMessage = t.message ?: "Error"
                }
            })
    }

    fun submitPermission(
        context: Context,
        scheduleId: Int,
        status: String,
        description: String,
        file: File,
        onSuccess: () -> Unit
    ) {
        val sessionManager = SessionManager(context)
        val token = sessionManager.fetchAuthToken() ?: return

        isLoading = true
        val bearer = "Bearer $token"

        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("photo", file.name, requestFile)

        val idBody = scheduleId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val statusBody = status.toRequestBody("text/plain".toMediaTypeOrNull())
        val descBody = description.toRequestBody("text/plain".toMediaTypeOrNull())

        RetrofitClient.instance.submitPermission(bearer, idBody, statusBody, descBody, imagePart)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    isLoading = false
                    if (response.isSuccessful) {
                        onSuccess()
                    } else {
                        val errorJsonString = response.errorBody()?.string()
                        val errorMessageFromServer = try {
                            val jsonObject = JSONObject(errorJsonString.toString())
                            jsonObject.getString("message")
                        } catch (e: Exception) {
                            "Gagal Izin: ${response.code()}"
                        }
                        errorMessage = errorMessageFromServer
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    isLoading = false
                    errorMessage = t.message ?: "Koneksi Error"
                }
            })
    }

    fun getCurrentUser(context: Context){
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
                        userData = body
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

    fun getLatestActivity(context: Context){
        val sessionManager = SessionManager(context)
        val token = sessionManager.fetchAuthToken()

        if (token == null){
            errorMessage = "Token tidak ditemukan"
            return
        }

        isLoading = true
        errorMessage = ""
        val bearer = "Bearer $token"

        RetrofitClient.instance.getLatestActivity(bearer).enqueue(object: Callback<LatestActivityResponse> {
            override fun onResponse(
                call: Call<LatestActivityResponse>,
                response: Response<LatestActivityResponse>
            ) {
                isLoading = false
                if (response.isSuccessful){
                    val body = response.body()
                    if(body != null){
                        activityData = body.data?.firstOrNull()
                        Log.d("LatestActivity", "Data: $body")
                    }else{
                        errorMessage = "Data tidak ditemukan"
                    }
                }else{
                    errorMessage = "Gagal mengambil data: ${response.code()}"
                }
            }

            override fun onFailure(
                call: Call<LatestActivityResponse>,
                t: Throwable
            ) {
                isLoading = false
                errorMessage = "Terjadi Kesalahan: ${t.message}"
            }
        })
    }

}
