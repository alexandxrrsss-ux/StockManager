package com.example.stockmanager

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey val codigo: String,
    val nombre: String
)

@Entity(
    tableName = "movimientos",
    foreignKeys = [ForeignKey(
        entity = Producto::class,
        parentColumns = ["codigo"],
        childColumns = ["codigoProducto"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("codigoProducto"), Index("sincronizado")]
)
data class Movimiento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val codigoProducto: String,
    val cantidad: Int,
    val tipo: String,          // "ENTRADA" o "SALIDA"
    val fecha: Long,           // System.currentTimeMillis()
    val sincronizado: Boolean = false
)
