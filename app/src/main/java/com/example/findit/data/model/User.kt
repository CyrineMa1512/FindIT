package com.example.findit.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val role: String = "etudiant" // "etudiant", "responsable", ou "admin"
)
