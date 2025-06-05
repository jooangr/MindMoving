package com.example.mindmoving.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    // Esta constante define la URL base del backend
    private const val BASE_URL = "https://mindmoving-api.onrender.com/api/"

    private val retrofit: Retrofit by lazy { // 'by lazy' para que la instancia se cree una sola vez y solo cuando se necesite
        Retrofit.Builder()
            .baseUrl(BASE_URL) //le indicamos la URL base de nuestra API
            .addConverterFactory(GsonConverterFactory.create()) // Convertimos JSON a objetos Kotlin (y viceversa)
            .build()
    }

    fun getApiService(): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
