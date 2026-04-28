package com.example.preventihome.data.repository

import com.example.preventihome.domain.model.Consulta
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio de consultas fisioterapéuticas.
 *
 * Gestiona todas las operaciones CRUD sobre la colección "consultas" en Firestore.
 * Separa las operaciones según el rol del usuario:
 * - Fisioterapeuta: crear, actualizar notas, ver por paciente
 * - Paciente: ver sus propias consultas activas
 *
 * Patrón Repository: abstrae Firestore del ViewModel,
 * facilitando pruebas y cambios futuros de fuente de datos.
 */
@Singleton
class ConsultaRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    /** UID del usuario autenticado actualmente */
    private val userId get() = auth.currentUser?.uid ?: ""

    /**
     * Crea una nueva consulta en Firestore.
     * Solo el fisioterapeuta autenticado puede crearla.
     *
     * @param consulta Objeto Consulta con todos los datos del diagnóstico
     * @return Result<String> con el ID del documento creado
     */
    suspend fun crearConsulta(consulta: Consulta): Result<String> = runCatching {
        val data = mapOf(
            "pacienteId"     to consulta.pacienteId,
            "fisioId"        to userId,
            "pacienteNombre" to consulta.pacienteNombre,
            "pacienteEmail"  to consulta.pacienteEmail,
            "fecha"          to consulta.fecha,
            "patologia"      to consulta.patologia,
            "diagnostico"    to consulta.diagnostico,
            "notas"          to consulta.notas,
            "ejerciciosIds"  to consulta.ejerciciosIds,
            "estado"         to "activa"
        )
        val docRef = db.collection("consultas").add(data).await()
        docRef.id
    }

    /**
     * Obtiene todas las consultas de un paciente específico.
     * Usado por el fisioterapeuta para ver el historial del paciente.
     *
     * @param pacienteId UID del paciente
     * @return Result<List<Consulta>> ordenadas por fecha descendente
     */
    suspend fun getConsultasPorPaciente(
        pacienteId: String
    ): Result<List<Consulta>> = runCatching {
        val snapshot = db.collection("consultas")
            .whereEqualTo("pacienteId", pacienteId)
            .whereEqualTo("fisioId", userId)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .get()
            .await()
        snapshot.documents.map { doc -> doc.toConsulta() }
    }

    /**
     * Obtiene las consultas activas del paciente autenticado.
     * Usado en el home del paciente para ver su diagnóstico actual.
     *
     * @return Result<List<Consulta>> solo consultas con estado "activa"
     */
    suspend fun getMisConsultas(): Result<List<Consulta>> = runCatching {
        val snapshot = db.collection("consultas")
            .whereEqualTo("pacienteId", userId)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .get()
            .await()
        snapshot.documents.map { doc -> doc.toConsulta() }
    }

    /**
     * Actualiza las notas de seguimiento de una consulta existente.
     * El fisioterapeuta puede actualizar las notas después de cada sesión.
     *
     * @param consultaId ID del documento en Firestore
     * @param notas      Nuevas notas de seguimiento
     */
    suspend fun actualizarNotas(
        consultaId: String,
        notas: String
    ): Result<Unit> = runCatching {
        db.collection("consultas")
            .document(consultaId)
            .update("notas", notas)
            .await()
    }

    /**
     * Cierra una consulta cambiando su estado a "cerrada".
     * Una consulta cerrada sigue siendo visible pero no editable.
     *
     * @param consultaId ID del documento a cerrar
     */
    suspend fun cerrarConsulta(consultaId: String): Result<Unit> = runCatching {
        db.collection("consultas")
            .document(consultaId)
            .update("estado", "cerrada")
            .await()
    }

    /**
     * Extensión privada para convertir un DocumentSnapshot de Firestore
     * al modelo de dominio Consulta.
     */
    @Suppress("UNCHECKED_CAST")
    private fun com.google.firebase.firestore.DocumentSnapshot.toConsulta() = Consulta(
        id             = id,
        pacienteId     = getString("pacienteId") ?: "",
        fisioId        = getString("fisioId") ?: "",
        pacienteNombre = getString("pacienteNombre") ?: "",
        pacienteEmail  = getString("pacienteEmail") ?: "",
        fecha          = getLong("fecha") ?: 0L,
        patologia      = getString("patologia") ?: "",
        diagnostico    = getString("diagnostico") ?: "",
        notas          = getString("notas") ?: "",
        ejerciciosIds  = (get("ejerciciosIds") as? List<String>) ?: emptyList(),
        estado         = getString("estado") ?: "activa"
    )

    /**
     * Obtiene la consulta activa más reciente del paciente autenticado.
     * Se usa para determinar la patología actual y filtrar ejercicios.
     *
     * @return Result<Consulta?> la consulta activa más reciente, o null si no hay ninguna
     */
    suspend fun getConsultaActivaActual(): Result<Consulta?> = runCatching {
        val snapshot = db.collection("consultas")
            .whereEqualTo("pacienteId", userId)
            .whereEqualTo("estado", "activa")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .await()
        snapshot.documents.firstOrNull()?.toConsulta()
    }
}