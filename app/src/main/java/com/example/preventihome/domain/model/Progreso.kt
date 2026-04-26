package com.example.preventihome.domain.model

data class Progreso(
    val id: String = "",
    val ejercicioId: String = "",
    val ejercicioNombre: String = "",
    val zona: String = "",
    val fecha: Long = System.currentTimeMillis(),
    val duracionSegundos: Int = 0,
    val evaluacion: String = "",  // "bien", "regular", "mal"
    val series: Int = 0,
    val repeticiones: Int = 0,
    val userId: String = ""
)