package com.example.preventihome.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de dependencias de la aplicación.
 *
 * Aquí se definen las instancias que Hilt va a proporcionar
 * en toda la app (Dependency Injection).
 *
 * Este módulo vive en el contenedor SingletonComponent,
 * lo que significa que las dependencias tendrán ciclo de vida global
 * (una sola instancia durante toda la ejecución de la app).
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Proporciona una instancia de FirebaseAuth.
     *
     * - Se usa para autenticación (login, registro, logout)
     * - Se declara como @Singleton para reutilizar la misma instancia
     *   en toda la aplicación
     *
     * @return Instancia única de FirebaseAuth
     */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Proporciona una instancia de FirebaseFirestore.
     *
     * - Se usa para operaciones de base de datos (lectura/escritura)
     * - Se comparte como singleton en toda la app
     *
     * @return Instancia única de FirebaseFirestore
     */
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}