package com.example.mindmoving.retrofit

import com.example.mindmoving.retrofit.models.GenericResponse
import com.example.mindmoving.retrofit.models.LoginRequest
import com.example.mindmoving.retrofit.models.LoginResponse
import com.example.mindmoving.retrofit.models.RegisterRequest
import com.example.mindmoving.retrofit.models.SesionEEGRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/api/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<GenericResponse>

    @POST("/api/sesiones")
    suspend fun guardarSesion(@Body sesion: SesionEEGRequest): Response<GenericResponse>
}
