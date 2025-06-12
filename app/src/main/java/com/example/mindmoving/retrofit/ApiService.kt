package com.example.mindmoving.retrofit

import com.example.mindmoving.retrofit.models.ActualizarUsuarioRequest
import com.example.mindmoving.retrofit.models.GenericResponse
import com.example.mindmoving.retrofit.models.LoginRequest
import com.example.mindmoving.retrofit.models.LoginResponse
import com.example.mindmoving.retrofit.models.PerfilCalibracionRequest
import com.example.mindmoving.retrofit.models.PerfilCalibracionResponse
import com.example.mindmoving.retrofit.models.RegisterRequest
import com.example.mindmoving.retrofit.models.SesionEEGRequest
import com.example.mindmoving.retrofit.models.UsuarioResponse
import com.example.mindmoving.retrofit.models.VerificarPasswordRequest
import com.example.mindmoving.retrofit.models.VerificarPasswordResponse
import com.example.mindmoving.retrofit.models.sesionesEGG.SesionEEGResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // Validar credenciales en el login
    @POST("/api/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    // Registrarse por primera vez
    @POST("/api/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<GenericResponse>

    //Crear Sesion
    @POST("/api/sesiones")
    suspend fun crearSesionEEG(@Body sesion: SesionEEGRequest): Response<Void>

    //Recibir sesiones del BACKEND
    @GET("/api/sesiones/{userId}")
    suspend fun getSesiones(@Path("userId") userId: String): Response<List<SesionEEGResponse>>

    //Obtiene el perfil de calibración del usuario. Se usa tras login para personalizar la calibración y análisis EEG.
    @GET("/api/perfil/{usuarioId}")
    suspend fun getPerfil(@Path("usuarioId") usuarioId: String): Response<PerfilCalibracionResponse>

    @POST("/api/perfil")
    suspend fun crearPerfil(@Body perfil: PerfilCalibracionRequest): Response<GenericResponse>

    @POST("/api/verificar-password/{id}")//editarpefil y register
    suspend fun verificarPasswordEditarPerfil(
        @Path("id") id: String,
        @Body request: VerificarPasswordRequest
    ): Response<GenericResponse>

    @PATCH("/api/update-user/{userId}")
    suspend fun actualizarUsuario(
        @Path("userId") userId: String,
        @Body request: ActualizarUsuarioRequest
    ): Response<GenericResponse>

    @GET("/api/users/{id}")
    suspend fun getUsuario(@Path("id") id: String): Response<UsuarioResponse>

    //theme
    @GET("users/{id}/theme")
    fun getTheme(@Path("id") userId: String): Call<ThemeResponse>

    @PUT("users/{id}/theme")
    fun updateTheme(@Path("id") userId: String, @Body body: ThemeRequest): Call<Void>

    data class ThemeResponse(val theme: String)
    data class ThemeRequest(val theme: String)
}
