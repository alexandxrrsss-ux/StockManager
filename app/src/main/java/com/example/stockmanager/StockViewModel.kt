package com.example.stockmanager

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StockViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).stockDao()

    val movimientos = dao.observarMovimientos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var mensaje by mutableStateOf("")
        private set

    var existencia by mutableStateOf<Int?>(null)
        private set

    // CREATE / UPDATE
    fun guardar(codigo: String, cantidadTxt: String, tipo: String, editando: Movimiento?) {
        val cantidad = cantidadTxt.toIntOrNull()
        if (codigo.isBlank() || cantidad == null || cantidad <= 0) {
            mensaje = "Captura un codigo y una cantidad valida (> 0)"
            return
        }
        viewModelScope.launch {
            if (editando == null) {
                dao.insertarProducto(Producto(codigo.trim(), codigo.trim()))
                dao.insertarMovimiento(
                    Movimiento(
                        codigoProducto = codigo.trim(),
                        cantidad = cantidad,
                        tipo = tipo,
                        fecha = System.currentTimeMillis()
                    )
                )
                mensaje = "Movimiento registrado"
            } else {
                // al editar se vuelve a marcar como pendiente de sincronizar
                dao.actualizarMovimiento(
                    editando.copy(cantidad = cantidad, tipo = tipo, sincronizado = false)
                )
                mensaje = "Movimiento actualizado"
            }
            existencia = dao.existencia(codigo.trim())
        }
    }

    // DELETE
    fun eliminar(m: Movimiento) {
        viewModelScope.launch {
            dao.eliminarMovimiento(m)
            existencia = dao.existencia(m.codigoProducto)
            mensaje = "Movimiento eliminado"
        }
    }

    // Consulta de existencia (SUM en SQL)
    fun consultarExistencia(codigo: String) {
        if (codigo.isBlank()) return
        viewModelScope.launch { existencia = dao.existencia(codigo.trim()) }
    }

    // Sincronizacion: solo corre cuando hay internet
    fun sincronizar() {
        val req = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            ).build()
        WorkManager.getInstance(getApplication()).enqueue(req)
        mensaje = "Sincronizacion programada (se ejecuta con internet)"
    }
}
