package com.example.absensijumat.ui.yasin

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.absensijumat.network.RetrofitClient
import com.example.absensijumat.response.YasinData
import com.example.absensijumat.response.YasinResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class YasinViewModel : ViewModel() {
    var yasinData by mutableStateOf<YasinData?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")

    private var mediaPlayer: MediaPlayer? = null
    var isPlaying by mutableStateOf(false)
    var currentlyPlayingAyat by mutableIntStateOf(-1) // 0 untuk full audio, 1+ untuk nomor ayat

    fun fetchYasin() {
        isLoading = true
        errorMessage = ""
        RetrofitClient.instance.getYasin().enqueue(object : Callback<YasinResponse> {
            override fun onResponse(call: Call<YasinResponse>, response: Response<YasinResponse>) {
                isLoading = false
                if (response.isSuccessful) {
                    yasinData = response.body()?.data
                } else {
                    errorMessage = "Gagal memuat Surah Yasin: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<YasinResponse>, t: Throwable) {
                isLoading = false
                errorMessage = t.message ?: "Terjadi kesalahan koneksi"
            }
        })
    }

    fun playAudio(url: String, ayatNumber: Int) {
        if (currentlyPlayingAyat == ayatNumber && isPlaying) {
            stopAudio()
            return
        }

        stopAudio()
        currentlyPlayingAyat = ayatNumber
        
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(url)
            prepareAsync()
            setOnPreparedListener { 
                start()
                this@YasinViewModel.isPlaying = true
            }
            setOnCompletionListener { 
                this@YasinViewModel.isPlaying = false
                currentlyPlayingAyat = -1
            }
        }
    }

    fun stopAudio() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
        isPlaying = false
        currentlyPlayingAyat = -1
    }

    override fun onCleared() {
        super.onCleared()
        stopAudio()
    }
}
