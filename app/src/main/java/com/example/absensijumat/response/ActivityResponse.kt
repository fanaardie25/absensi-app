package com.example.absensijumat.response

data class ActivityResponse(
    val success: Boolean,
    val data: List<AttendanceDataAll>?,
)

data class AttendanceDataAll(
    val id: Int,
    val status: String,
    val photo_path: String,
    val date: String,
)
