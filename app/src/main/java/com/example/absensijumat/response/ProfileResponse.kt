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
    val agenda_name: String,
    val start_absensi: String,
    val end_absensi: String,
    val school_class: SchoolClass,
    val teacher: String?,
    val is_schedule_open: Boolean,
    val is_absent_today: Boolean,
    val stats: Stats,
    val must_change_password: Boolean
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
    val tidak_hadir: Int,
)