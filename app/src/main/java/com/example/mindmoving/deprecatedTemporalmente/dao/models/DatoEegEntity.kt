package com.example.mindmoving.deprecatedTemporalmente.dao.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "datos_eeg",
    foreignKeys = [
        ForeignKey(
            entity = SesionEegEntity::class,
            parentColumns = ["id"],
            childColumns = ["sesionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sesionId")]
)
data class DatoEegEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sesionId: Int,
    val tiempo: Int,
    val atencion: Int,
    val meditacion: Int,
    val pestaneo: Int,
    val senal: Int
)
