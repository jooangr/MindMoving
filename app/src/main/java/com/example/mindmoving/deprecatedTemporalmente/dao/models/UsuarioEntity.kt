package com.example.mindmoving.deprecatedTemporalmente.dao.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val email: String,
    val passwordHash: String,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val ultimoLoginExitoso: Long? = null,
    val rol: String = "usuario"
)
