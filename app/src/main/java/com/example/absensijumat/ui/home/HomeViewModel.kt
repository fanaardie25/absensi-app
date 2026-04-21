package com.example.absensijumat.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.absensijumat.network.RetrofitClient
import com.example.absensijumat.response.AttendanceData
import com.example.absensijumat.response.LatestActivityResponse
import com.example.absensijumat.response.ProfileResponse
import com.example.absensijumat.utils.ErrorHandler
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
    var successMessage by mutableStateOf("")

    var activityData by mutableStateOf<AttendanceData?>(null)

    fun clearError() {
        errorMessage = ""
    }

    fun clearSuccess() {
        successMessage = ""
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
                errorMessage = "Gagal mendapatkan lokasi. Pastikan GPS kamu aktif."
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

        // 1. Ubah Bitmap ke File
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
                        Log.d("success",response.isSuccessful.toString())
                        onAttendanceSuccess()
                    } else {
                        val errorJsonString = response.errorBody()?.string()
                        Log.d("AttendanceResponse", "Error Body: $errorJsonString")
                        val errorMessageFromServer = try {
                            val jsonObject = JSONObject(errorJsonString)
                            jsonObject.getString("message")
                        } catch (e: Exception) {
                            ErrorHandler.getFriendlyMessage(response.code())
                        }
                        errorMessage = errorMessageFromServer
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    isLoading = false
                    errorMessage = ErrorHandler.getFriendlyMessage(t)
                }
            })
    }

    fun getCurrentUser(context: Context){
        val sessionManager = SessionManager(context)
        val token = sessionManager.fetchAuthToken()
        if(token == null){
            errorMessage = "Sesi telah berakhir. Silakan login kembali."
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
                Log.d("AttendanceResponse", "KODE ASLI: ${response.code()}")
                if (response.isSuccessful){
                    val body = response.body()
                    if(body != null){
                        userData = body
                    }else{
                        errorMessage = "Gagal memuat profil. Silakan coba lagi."
                    }
                }else{
                    errorMessage = ErrorHandler.getFriendlyMessage(response.code())
                }
            }

            override fun onFailure(
                call: Call<ProfileResponse?>,
                t: Throwable?
            ) {
                isLoading = false
                errorMessage = ErrorHandler.getFriendlyMessage(t)
            }
        })
    }

    fun getLatestActivity(context: Context){
        val sessionManager = SessionManager(context)
        val token = sessionManager.fetchAuthToken()

        if (token == null){
            errorMessage = "Sesi telah berakhir."
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
                        // Tidak ada aktivitas bukan berarti error
                    }
                }else{
                    errorMessage = ErrorHandler.getFriendlyMessage(response.code())
                }
            }

            override fun onFailure(
                call: Call<LatestActivityResponse>,
                t: Throwable
            ) {
                isLoading = false
                errorMessage = ErrorHandler.getFriendlyMessage(t)
            }
        })
    }

}
