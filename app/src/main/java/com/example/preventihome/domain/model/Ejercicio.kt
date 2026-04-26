package com.example.preventihome.domain.model

data class Ejercicio(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val zona: String = "",
    val tipo: String = "",
    val dificultad: Int = 1,
    val series: Int = 3,
    val repeticiones: Int = 10,
    val imagenUrl: String = "",
    val activo: Boolean = true,
    val chefRecomendado: Boolean = false
)