package com.example.absensijumat.utils

import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ErrorHandler {
    fun getFriendlyMessage(t: Throwable?): String {
        return when (t) {
            is UnknownHostException -> "Koneksi internet tidak tersedia. Pastikan WiFi atau Data Seluler kamu aktif."
            is ConnectException -> "Gagal terhubung ke server. Silakan periksa koneksi internet kamu."
            is SocketTimeoutException -> "Waktu permintaan habis. Server sedang sibuk, silakan coba lagi nanti."
            else -> "Terjadi kesalahan sistem. Silakan coba beberapa saat lagi."
        }
    }

    fun getFriendlyMessage(code: Int): String {
        return when (code) {
            401 -> "Sesi telah berakhir. Silakan login kembali."
            403 -> "Akses ditolak. Kamu tidak memiliki izin untuk melakukan ini."
            404 -> "Data tidak ditemukan."
            500 -> "Terjadi masalah pada server. Tim teknis kami sedang memperbaikinya."
            else -> "Terjadi kesalahan (Kode: $code). Silakan coba lagi nanti."
        }
    }
}
