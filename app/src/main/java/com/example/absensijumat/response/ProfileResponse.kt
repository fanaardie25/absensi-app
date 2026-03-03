package com.example.absensijumat.response

data class ProfileResponse(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val nis: String,
    val class_id: Int,
    val profile_photo_path: String,
    val school_class: SchoolClass
)

data class SchoolClass(
    val id: Int,
    val name: String,
    val grade: String,
    val major: String,
    val sequence: String
)