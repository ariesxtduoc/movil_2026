package com.example.android_16.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OrdenTrabajoDao {

    @Query("SELECT * FROM ordenes_trabajo ORDER BY createdAt DESC")
    fun observarOrdenes(): Flow<List<OrdenTrabajoEntity>>

    @Query("SELECT * FROM ordenes_trabajo WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: String): OrdenTrabajoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(ordenes: List<OrdenTrabajoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUna(orden: OrdenTrabajoEntity)

    @Query("DELETE FROM ordenes_trabajo WHERE id = :id")
    suspend fun eliminarPorId(id: String)

    @Query("DELETE FROM ordenes_trabajo")
    suspend fun limpiarTodo()
}