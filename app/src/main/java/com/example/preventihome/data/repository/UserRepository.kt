package com.example.preventihome.data.repository

import com.example.preventihome.data.remote.FirestoreSource
import com.example.preventihome.domain.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestoreSource: FirestoreSource
) {
    suspend fun getPacientes(): Result<List<User>> = runCatching {
        firestoreSource.getUsersByRol("paciente")
    }

    suspend fun getAllUsers(): Result<List<User>> = runCatching {
        firestoreSource.getAllUsers()
    }

    suspend fun promoverAFisio(uid: String): Result<Unit> = runCatching {
        firestoreSource.updateUserRol(uid, "fisio")
    }

    suspend fun revocarFisio(uid: String): Result<Unit> = runCatching {
        firestoreSource.updateUserRol(uid, "paciente")
    }
}