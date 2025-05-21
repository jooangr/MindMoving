package com.example.mindmoving.deprecatedTemporalmente.dao.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "perfiles_calibracion",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("usuarioId")]
)
data class PerfilCalibracionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuarioId: Int,
    val nombrePerfil: String,
    val rangoAtencion: String,
    val rangoMeditacion: String,
    val rangoPestaneo: String,
    val activo: Boolean = false
)
