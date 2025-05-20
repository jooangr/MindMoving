package com.example.mindmoving.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mindmoving.dao.UsuarioDao
//import com.example.mindmoving.dao.PerfilCalibracionDao
//import com.example.mindmoving.dao.SesionEegDao
import com.example.mindmoving.models.UsuarioEntity
import com.example.mindmoving.models.PerfilCalibracionEntity
import com.example.mindmoving.models.SesionEegEntity
import com.example.mindmoving.models.DatoEegEntity

@Database(
    entities = [
        UsuarioEntity::class,
        PerfilCalibracionEntity::class,
        SesionEegEntity::class,
        DatoEegEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    //abstract fun perfilCalibracionDao(): PerfilCalibracionDAO
    //abstract fun sesionEegDao(): SesionEegDao
}
