package com.example.preventihome.domain.model

/**
 * Modelo de dominio que representa un ejercicio dentro de la aplicación.
 *
 * Este objeto contiene toda la información necesaria para mostrar,
 * clasificar y configurar un ejercicio físico.
 */
data class Ejercicio(

    /** Identificador único del ejercicio */
    val id: String = "",

    /** Nombre del ejercicio */
    val nombre: String = "",

    /** Descripción detallada de cómo realizar el ejercicio */
    val descripcion: String = "",

    /** Zona del cuerpo a la que está dirigido (ej: espalda, piernas, brazos) */
    val zona: String = "",

    /** Tipo de ejercicio (ej: fuerza, movilidad, rehabilitación, etc.) */
    val tipo: String = "",

    /** Nivel de dificultad del ejercicio (ej: 1 = fácil, 2 = medio, 3 = difícil) */
    val dificultad: Int = 1,

    /** Número de series recomendadas */
    val series: Int = 3,

    /** Número de repeticiones por serie */
    val repeticiones: Int = 10,

    /** URL de la imagen representativa del ejercicio */
    val imagenUrl: String = "",

    /** Indica si el ejercicio está activo y disponible en la app */
    val activo: Boolean = true,

    /**
     * Indica si el ejercicio es recomendado por el "chef"
     * (posiblemente una lógica especial o sección destacada en la app)
     */
    val chefRecomendado: Boolean = false
)