package com.example.preventihome.domain.model

data class User(
    val uid: String,
    val email: String,
    val nombre: String = "",
    val rol: String = "paciente"  // "paciente" | "fisio" | "admin"
)