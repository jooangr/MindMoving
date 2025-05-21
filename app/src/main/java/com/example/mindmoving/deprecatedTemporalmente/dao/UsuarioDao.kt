package com.example.mindmoving.deprecatedTemporalmente.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.mindmoving.deprecatedTemporalmente.dao.models.UsuarioEntity

@Dao
interface UsuarioDao {
    @Insert
    suspend fun insertar(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun buscarPorEmail(email: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun obtenerPorId(id: Int): UsuarioEntity
}
