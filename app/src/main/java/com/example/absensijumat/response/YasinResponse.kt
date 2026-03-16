package com.example.absensijumat.response

data class YasinResponse(
    val code: Int,
    val message: String,
    val data: YasinData
)

data class YasinData(
    val nomor: Int,
    val nama: String,
    val namaLatin: String,
    val jumlahAyat: Int,
    val arti: String,
    val audioFull: Map<String, String>,
    val ayat: List<Ayat>
)

data class Ayat(
    val nomorAyat: Int,
    val teksArab: String,
    val teksLatin: String,
    val teksIndonesia: String,
    val audio: Map<String, String>
)
