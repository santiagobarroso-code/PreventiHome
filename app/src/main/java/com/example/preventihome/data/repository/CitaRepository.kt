package com.example.preventihome.data.repository

import com.example.preventihome.domain.model.Cita
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio de citas médicas.
 *
 * Gestiona todas las operaciones sobre la colección "citas" en Firestore.
 * Separa las operaciones por rol:
 * - Paciente: agendar y ver sus citas
 * - Fisioterapeuta: ver citas pendientes y atenderlas
 */
@Singleton
class CitaRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    /** UID del usuario autenticado */
    private val userId get() = auth.currentUser?.uid ?: ""

    /**
     * Agenda una nueva cita desde el lado del paciente.
     * El estado inicial siempre es "pendiente".
     *
     * @param cita Objeto con los datos de la cita
     * @return Result<String> con el ID del documento creado
     */
    suspend fun agendarCita(cita: Cita): Result<String> = runCatching {
        val data = mapOf(
            "pacienteId"     to userId,
            "pacienteNombre" to cita.pacienteNombre,
            "pacienteEmail"  to cita.pacienteEmail,
            "fisioId"        to "",
            "fechaCita"      to cita.fechaCita,
            "motivo"         to cita.motivo,
            "estado"         to "pendiente",
            "consultaId"     to "",
            "creadaEn"       to System.currentTimeMillis()
        )
        val docRef = db.collection("citas").add(data).await()
        docRef.id
    }

    /**
     * Obtiene las citas del paciente autenticado.
     * Ordenadas por fecha de cita descendente.
     *
     * @return Result<List<Cita>> con todas las citas del paciente
     */
    suspend fun getMisCitas(): Result<List<Cita>> = runCatching {
        val snapshot = db.collection("citas")
            .whereEqualTo("pacienteId", userId)
            .orderBy("fechaCita", Query.Direction.DESCENDING)
            .get()
            .await()
        snapshot.documents.map { it.toCita() }
    }

    /**
     * Obtiene todas las citas pendientes para el fisioterapeuta.
     * El fisio ve citas de todos los pacientes sin importar el fisioId.
     *
     * @return Result<List<Cita>> con citas en estado "pendiente"
     */
    suspend fun getCitasPendientes(): Result<List<Cita>> = runCatching {
        val snapshot = db.collection("citas")
            .whereEqualTo("estado", "pendiente")
            .orderBy("fechaCita", Query.Direction.ASCENDING)
            .get()
            .await()
        snapshot.documents.map { it.toCita() }
    }

    /**
     * Marca una cita como atendida y la vincula con una consulta.
     * Se llama después de que el fisio crea la consulta correspondiente.
     *
     * @param citaId    ID de la cita a marcar como atendida
     * @param consultaId ID de la consulta creada para esta cita
     */
    suspend fun marcarComoAtendida(
        citaId: String,
        consultaId: String
    ): Result<Unit> = runCatching {
        db.collection("citas").document(citaId)
            .update(
                mapOf(
                    "estado"     to "atendida",
                    "fisioId"    to userId,
                    "consultaId" to consultaId
                )
            )
            .await()
    }

    /**
     * Cancela una cita cambiando su estado.
     * Puede ser llamado tanto por el paciente como por el fisio.
     *
     * @param citaId ID de la cita a cancelar
     */
    suspend fun cancelarCita(citaId: String): Result<Unit> = runCatching {
        db.collection("citas").document(citaId)
            .update("estado", "cancelada")
            .await()
    }

    /**
     * Extensión privada para convertir DocumentSnapshot al modelo Cita.
     */
    private fun com.google.firebase.firestore.DocumentSnapshot.toCita() = Cita(
        id             = id,
        pacienteId     = getString("pacienteId") ?: "",
        pacienteNombre = getString("pacienteNombre") ?: "",
        pacienteEmail  = getString("pacienteEmail") ?: "",
        fisioId        = getString("fisioId") ?: "",
        fechaCita      = getLong("fechaCita") ?: 0L,
        motivo         = getString("motivo") ?: "",
        estado         = getString("estado") ?: "pendiente",
        consultaId     = getString("consultaId") ?: "",
        creadaEn       = getLong("creadaEn") ?: 0L
    )
}