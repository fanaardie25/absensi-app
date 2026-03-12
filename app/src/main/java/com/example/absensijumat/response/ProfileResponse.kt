package com.example.absensijumat.response

data class ProfileResponse(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val nis: String,
    val class_id: Int,
    val profile_photo_path: String,
    val schedule_id: Int,
    val school_class: SchoolClass,
    val teacher: String?,
    val is_schedule_open: Boolean,
    val is_absent_today: Boolean,
    val stats: Stats
)

data class SchoolClass(
    val id: Int,
    val name: String,
    val grade: String,
    val major: String,
    val sequence: String
)

data class Stats(
    val hadir: Int,
    val total_pekan: Int,
)