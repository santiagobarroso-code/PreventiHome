package com.example.preventihome.data.repository

import com.example.preventihome.domain.model.Progreso
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgresoRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val userId get() = auth.currentUser?.uid ?: ""

    suspend fun guardarProgreso(progreso: Progreso): Result<Unit> = runCatching {
        val data = mapOf(
            "ejercicioId"       to progreso.ejercicioId,
            "ejercicioNombre"   to progreso.ejercicioNombre,
            "zona"              to progreso.zona,
            "fecha"             to progreso.fecha,
            "duracionSegundos"  to progreso.duracionSegundos,
            "evaluacion"        to progreso.evaluacion,
            "series"            to progreso.series,
            "repeticiones"      to progreso.repeticiones,
            "userId"            to userId
        )
        db.collection("usuarios")
            .document(userId)
            .collection("progreso")
            .add(data)
            .await()
        Unit
    }

    suspend fun getHistorial(): Result<List<Progreso>> = runCatching {
        val snapshot = db.collection("usuarios")
            .document(userId)
            .collection("progreso")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .get()
            .await()

        snapshot.documents.map { doc ->
            Progreso(
                id                = doc.id,
                ejercicioId       = doc.getString("ejercicioId") ?: "",
                ejercicioNombre   = doc.getString("ejercicioNombre") ?: "",
                zona              = doc.getString("zona") ?: "",
                fecha             = doc.getLong("fecha") ?: 0L,
                duracionSegundos  = (doc.getLong("duracionSegundos") ?: 0L).toInt(),
                evaluacion        = doc.getString("evaluacion") ?: "",
                series            = (doc.getLong("series") ?: 0L).toInt(),
                repeticiones      = (doc.getLong("repeticiones") ?: 0L).toInt(),
                userId            = doc.getString("userId") ?: ""
            )
        }
    }
}