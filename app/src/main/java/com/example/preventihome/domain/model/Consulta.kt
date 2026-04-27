package com.example.preventihome.domain.model

/**
 * Modelo de dominio que representa una consulta fisioterapéutica.
 *
 * Una consulta es creada por un fisioterapeuta para un paciente específico.
 * Contiene el diagnóstico, la patología identificada, los ejercicios asignados
 * y las notas de seguimiento. El paciente puede ver sus consultas y acceder
 * a los ejercicios que le fueron asignados.
 *
 * @param id              ID único del documento en Firestore (generado automáticamente)
 * @param pacienteId      UID del paciente en Firebase Auth
 * @param fisioId         UID del fisioterapeuta que crea la consulta
 * @param pacienteNombre  Nombre del paciente para mostrar en el panel del fisio
 * @param pacienteEmail   Correo del paciente
 * @param fecha           Timestamp de creación en milisegundos
 * @param patologia       Patología identificada (ej: "lumbalgia", "cervicalgia")
 * @param diagnostico     Texto libre con el diagnóstico del fisioterapeuta
 * @param notas           Notas de seguimiento actualizables
 * @param ejerciciosIds   Lista de IDs de ejercicios asignados de la colección ejercicios/
 * @param estado          Estado de la consulta: "activa" o "cerrada"
 */
data class Consulta(
    val id: String = "",
    val pacienteId: String = "",
    val fisioId: String = "",
    val pacienteNombre: String = "",
    val pacienteEmail: String = "",
    val fecha: Long = System.currentTimeMillis(),
    val patologia: String = "",
    val diagnostico: String = "",
    val notas: String = "",
    val ejerciciosIds: List<String> = emptyList(),
    val estado: String = "activa"
)