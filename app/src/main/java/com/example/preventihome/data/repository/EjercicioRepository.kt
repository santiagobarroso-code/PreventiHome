package com.example.preventihome.data.repository

import com.example.preventihome.domain.model.Ejercicio
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EjercicioRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    // Trae todos los ejercicios activos
    suspend fun getEjercicios(): Result<List<Ejercicio>> = runCatching {
        val snapshot = db.collection("ejercicios")
            .whereEqualTo("activo", true)
            .get()
            .await()
        snapshot.documents.map { doc ->
            Ejercicio(
                id = doc.id,
                nombre = doc.getString("nombre") ?: "",
                descripcion = doc.getString("descripcion") ?: "",
                zona = doc.getString("zona") ?: "",
                tipo = doc.getString("tipo") ?: "",
                dificultad = (doc.getLong("dificultad") ?: 1L).toInt(),
                series = (doc.getLong("series") ?: 3L).toInt(),
                repeticiones = (doc.getLong("repeticiones") ?: 10L).toInt(),
                imagenUrl = doc.getString("imagen_url") ?: "",
                activo = doc.getBoolean("activo") ?: true,
                chefRecomendado = doc.getBoolean("chef_recomendado") ?: false
            )
        }
    }

    // Trae ejercicios filtrados por zona
    suspend fun getEjerciciosPorZona(zona: String): Result<List<Ejercicio>> = runCatching {
        val snapshot = db.collection("ejercicios")
            .whereEqualTo("activo", true)
            .whereEqualTo("zona", zona)
            .get()
            .await()
        snapshot.documents.map { doc ->
            Ejercicio(
                id = doc.id,
                nombre = doc.getString("nombre") ?: "",
                descripcion = doc.getString("descripcion") ?: "",
                zona = doc.getString("zona") ?: "",
                tipo = doc.getString("tipo") ?: "",
                dificultad = (doc.getLong("dificultad") ?: 1L).toInt(),
                series = (doc.getLong("series") ?: 3L).toInt(),
                repeticiones = (doc.getLong("repeticiones") ?: 10L).toInt(),
                imagenUrl = doc.getString("imagen_url") ?: "",
                activo = doc.getBoolean("activo") ?: true,
                chefRecomendado = doc.getBoolean("chef_recomendado") ?: false
            )
        }
    }

    // Trae un ejercicio por ID
    suspend fun getEjercicioById(id: String): Result<Ejercicio> = runCatching {
        val doc = db.collection("ejercicios").document(id).get().await()
        Ejercicio(
            id = doc.id,
            nombre = doc.getString("nombre") ?: "",
            descripcion = doc.getString("descripcion") ?: "",
            zona = doc.getString("zona") ?: "",
            tipo = doc.getString("tipo") ?: "",
            dificultad = (doc.getLong("dificultad") ?: 1L).toInt(),
            series = (doc.getLong("series") ?: 3L).toInt(),
            repeticiones = (doc.getLong("repeticiones") ?: 10L).toInt(),
            imagenUrl = doc.getString("imagen_url") ?: "",
            activo = doc.getBoolean("activo") ?: true,
            chefRecomendado = doc.getBoolean("chef_recomendado") ?: false
        )
    }


    /**
     * Obtiene ejercicios filtrados por patología.
     * Usado cuando el paciente tiene una consulta activa con patología asignada,
     * para mostrarle los ejercicios más relevantes para su condición.
     *
     * @param patologia Patología de la consulta activa del paciente
     *                  (ej: "lumbalgia", "cervicalgia", "gonalgia")
     * @return Result<List<Ejercicio>> ejercicios que contienen la patología
     */
    suspend fun getEjerciciosPorPatologia(patologia: String): Result<List<Ejercicio>> = runCatching {
        val snapshot = db.collection("ejercicios")
            .whereEqualTo("activo", true)
            .whereArrayContains("patologias", patologia)
            .get()
            .await()
        snapshot.documents.map { doc ->
            Ejercicio(
                id              = doc.id,
                nombre          = doc.getString("nombre") ?: "",
                descripcion     = doc.getString("descripcion") ?: "",
                zona            = doc.getString("zona") ?: "",
                tipo            = doc.getString("tipo") ?: "",
                dificultad      = (doc.getLong("dificultad") ?: 1L).toInt(),
                series          = (doc.getLong("series") ?: 3L).toInt(),
                repeticiones    = (doc.getLong("repeticiones") ?: 10L).toInt(),
                imagenUrl       = doc.getString("imagen_url") ?: "",
                activo          = doc.getBoolean("activo") ?: true,
                chefRecomendado = doc.getBoolean("chef_recomendado") ?: false
            )
        }
    }
}