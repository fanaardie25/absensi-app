package com.example.absensijumat.response

data class LatestActivityResponse(
    val success: Boolean,
    val data: List<AttendanceData>?,
)

data class AttendanceData(
    val id: Int,
    val status: String,
    val date: String,
)
