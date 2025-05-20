package com.example.mindmoving.deprecatedTemporalmente.dao.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sesiones_eeg",
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
data class SesionEegEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuarioId: Int,
    val fechaHora: Long,
    val duracion: Int,
    val valorMedioAtencion: Int,
    val valorMedioRelajacion: Int,
    val valorMedioPestaneo: Int,
    val comandosEjecutados: String
)
