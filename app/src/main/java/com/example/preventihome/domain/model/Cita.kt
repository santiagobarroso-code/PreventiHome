package com.example.preventihome.domain.model

/**
 * Modelo de dominio que representa una cita agendada por un paciente.
 *
 * El paciente agenda la cita con un motivo y fecha preferida.
 * El fisioterapeuta la ve en su panel y puede convertirla en consulta
 * agregando diagnóstico y patología.
 *
 * Estados posibles:
 * - "pendiente": recién agendada, esperando atención del fisio
 * - "atendida":  el fisio la convirtió en consulta
 * - "cancelada": cancelada por el paciente o el fisio
 *
 * @param id             ID único del documento en Firestore
 * @param pacienteId     UID del paciente que agendó la cita
 * @param pacienteNombre Nombre del paciente para mostrar en el panel del fisio
 * @param pacienteEmail  Correo del paciente
 * @param fisioId        UID del fisio asignado (vacío si aún no hay)
 * @param fechaCita      Timestamp de la fecha preferida de la cita
 * @param motivo         Descripción del motivo de la consulta
 * @param estado         Estado actual: "pendiente", "atendida" o "cancelada"
 * @param consultaId     ID de la consulta generada (se llena al atender)
 * @param creadaEn       Timestamp de cuando se agendó la cita
 */
data class Cita(
    val id: String = "",
    val pacienteId: String = "",
    val pacienteNombre: String = "",
    val pacienteEmail: String = "",
    val fisioId: String = "",
    val fechaCita: Long = System.currentTimeMillis(),
    val motivo: String = "",
    val estado: String = "pendiente",
    val consultaId: String = "",
    val creadaEn: Long = System.currentTimeMillis()
)