package com.example.stockmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

class SyncWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val dao = AppDatabase.get(applicationContext).stockDao()
        val pendientes = dao.obtenerPendientes()
        if (pendientes.isEmpty()) return Result.success()
        return try {
            // Aqui iria la llamada Retrofit al servidor central.
            // Se SIMULA el envio con un retraso porque no hay API real.
            delay(1000)
            dao.marcarSincronizados(pendientes.map { it.id })
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
