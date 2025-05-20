package com.example.mindmoving.retrofit

import com.example.mindmoving.retrofit.modelsDate.GenericResponse
import com.example.mindmoving.retrofit.modelsDate.LoginRequest
import com.example.mindmoving.retrofit.modelsDate.LoginResponse
import com.example.mindmoving.retrofit.modelsDate.RegisterRequest
import com.example.mindmoving.retrofit.modelsDate.SesionEEGRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/api/register")
    suspend fun register(@Body request: RegisterRequest): Response<GenericResponse>

    @POST("/api/sesiones")
    suspend fun guardarSesion(@Body sesion: SesionEEGRequest): Response<GenericResponse>
}
