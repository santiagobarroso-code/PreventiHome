package com.example.preventihome.domain.model

/**
 * Modelo de dominio que representa el progreso de un usuario
 * al realizar un ejercicio.
 *
 * Este objeto permite registrar métricas de desempeño,
 * historial y evaluación del ejercicio realizado.
 */
data class Progreso(

    /** Identificador único del registro de progreso */
    val id: String = "",

    /** ID del ejercicio al que pertenece este progreso */
    val ejercicioId: String = "",

    /** Nombre del ejercicio (útil para evitar consultas adicionales) */
    val ejercicioNombre: String = "",

    /** Zona del cuerpo trabajada en el ejercicio */
    val zona: String = "",

    /**
     * Fecha en la que se realizó el ejercicio.
     * Se guarda como timestamp en milisegundos.
     */
    val fecha: Long = System.currentTimeMillis(),

    /** Duración total del ejercicio en segundos */
    val duracionSegundos: Int = 0,

    /**
     * Evaluación del desempeño del usuario.
     * Valores esperados:
     * - "bien"
     * - "regular"
     * - "mal"
     */
    val evaluacion: String = "",

    /** Número de series realizadas */
    val series: Int = 0,

    /** Número de repeticiones realizadas por serie */
    val repeticiones: Int = 0,

    /** ID del usuario al que pertenece este progreso */
    val userId: String = ""
)