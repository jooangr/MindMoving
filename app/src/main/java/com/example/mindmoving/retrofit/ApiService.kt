package com.example.mindmoving.retrofit

import com.example.mindmoving.retrofit.models.user.ActualizarUsuarioRequest
import com.example.mindmoving.retrofit.models.GenericResponse
import com.example.mindmoving.retrofit.models.login_register.LoginRequest
import com.example.mindmoving.retrofit.models.login_register.LoginResponse
import com.example.mindmoving.retrofit.models.perfilCalibracion.PerfilCalibracionRequest
import com.example.mindmoving.retrofit.models.perfilCalibracion.PerfilCalibracionResponse
import com.example.mindmoving.retrofit.models.login_register.RegisterRequest
import com.example.mindmoving.retrofit.models.sesionesEGG.SesionEEGRequest
import com.example.mindmoving.retrofit.models.user.UsuarioResponse
import com.example.mindmoving.retrofit.models.verificarPassword.VerificarPasswordRequest
import com.example.mindmoving.retrofit.models.sesionesEGG.SesionEEGResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
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

    //Eliminar Usuario
    @DELETE("users/{id}")
    suspend fun eliminarUsuario(
        @Path("id") userId: String,
        @Query("password") password: String
    ): Response<Void>

    @PATCH("perfil/{id}/tipo")
    suspend fun actualizarTipoPerfil(
        @Path("id") userId: String,
        @Body body: Map<String, String>
    ): Response<PerfilCalibracionResponse>

    @POST("perfil/perfil/{id}/predefinido")
    suspend fun crearPerfilPredefinido(
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): Response<Any>

    @POST("/api/perfil/save")
    suspend fun actualizarPerfil(@Body perfil: PerfilCalibracionRequest): Response<GenericResponse>


    //Obtiene el perfil de calibración del usuario. Se usa tras login para personalizar la calibración y análisis EEG.
    @GET("/api/perfil/{usuarioId}")
    suspend fun getPerfil(@Path("usuarioId") usuarioId: String): Response<PerfilCalibracionResponse>

    @POST("/api/perfil")
    suspend fun crearPerfil(@Body perfil: PerfilCalibracionRequest): Response<GenericResponse>

    // Registrarse por primera vez
    @POST("/api/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<GenericResponse>

    //Crear Sesion
    @POST("/api/sesiones")
    suspend fun crearSesionEEG(@Body sesion: SesionEEGRequest): Response<Void>

    //Recibir sesiones del BACKEND
    @GET("/api/sesiones/{userId}")
    suspend fun getSesiones(@Path("userId") userId: String): Response<List<SesionEEGResponse>>

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
