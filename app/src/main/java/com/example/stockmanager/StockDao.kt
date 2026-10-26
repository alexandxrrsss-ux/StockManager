package com.example.stockmanager

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {
    // ---------- CREATE ----------
    // IGNORE (no REPLACE): REPLACE borraria el producto y, por CASCADE, sus movimientos.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarProducto(p: Producto)

    @Insert
    suspend fun insertarMovimiento(m: Movimiento): Long

    @Insert
    suspend fun insertarMovimientos(lista: List<Movimiento>) // lote en una sola transaccion

    // ---------- READ ----------
    @Query("SELECT * FROM movimientos ORDER BY fecha DESC")
    fun observarMovimientos(): Flow<List<Movimiento>>

    @Query("SELECT * FROM movimientos WHERE sincronizado = 0")
    suspend fun obtenerPendientes(): List<Movimiento>

    @Query("SELECT * FROM movimientos WHERE codigoProducto = :codigo ORDER BY fecha DESC")
    suspend fun obtenerPorProducto(codigo: String): List<Movimiento>

    // Agregacion en SQL (no en Kotlin) usando el indice de codigoProducto
    @Query(
        """SELECT COALESCE(SUM(CASE WHEN tipo = 'ENTRADA' THEN cantidad ELSE -cantidad END), 0)
           FROM movimientos WHERE codigoProducto = :codigo"""
    )
    suspend fun existencia(codigo: String): Int

    // ---------- UPDATE ----------
    @Update
    suspend fun actualizarMovimiento(m: Movimiento)

    @Query("UPDATE movimientos SET sincronizado = 1 WHERE id IN (:ids)")
    suspend fun marcarSincronizados(ids: List<Long>)

    // ---------- DELETE ----------
    @Delete
    suspend fun eliminarMovimiento(m: Movimiento)

    @Query("DELETE FROM movimientos WHERE sincronizado = 1 AND fecha < :limite")
    suspend fun limpiarSincronizados(limite: Long)
}
