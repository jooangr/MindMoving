package com.example.mindmoving.retrofit

import com.example.mindmoving.retrofit.models.ActualizarUsuarioRequest
import com.example.mindmoving.retrofit.models.GenericResponse
import com.example.mindmoving.retrofit.models.LoginRequest
import com.example.mindmoving.retrofit.models.LoginResponse
import com.example.mindmoving.retrofit.models.PerfilCalibracionResponse
import com.example.mindmoving.retrofit.models.RegisterRequest
import com.example.mindmoving.retrofit.models.SesionEEGRequest
import com.example.mindmoving.retrofit.models.VerificarPasswordResponse
import com.example.mindmoving.retrofit.models.sesionesEGG.SesionEEGResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("/api/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<GenericResponse>

    @POST("/api/sesiones")
    suspend fun guardarSesion(@Body sesion: SesionEEGRequest): Response<GenericResponse>

    //Recibir sesiones del back
    @GET("/api/sesiones/{userId}")
    suspend fun getSesiones(@Path("userId") userId: String): Response<List<SesionEEGResponse>>

    //Obtiene el perfil de calibración del usuario. Se usa tras login para personalizar la calibración y análisis EEG.
    @GET("/api/perfil/{usuarioId}")
    suspend fun getPerfil(@Path("usuarioId") usuarioId: String): Response<PerfilCalibracionResponse>

    @POST("/api/verify-password")
    suspend fun verificarPassword(
        @Query("userId") userId: String,
        @Body password: String
    ): Response<VerificarPasswordResponse>

    @PATCH("/api/update-user/{userId}")
    suspend fun actualizarUsuario(
        @Path("userId") userId: String,
        @Body request: ActualizarUsuarioRequest
    ): Response<GenericResponse>



}
