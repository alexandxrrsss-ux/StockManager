package com.example.stockmanager

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StockDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: StockDao

    @Before fun crear() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java
        ).build()
        dao = db.stockDao()
    }

    @After fun cerrar() = db.close()

    @Test fun insertaYCalculaExistencia() = runBlocking {
        dao.insertarProducto(Producto("P001", "Caja"))
        dao.insertarMovimiento(Movimiento(codigoProducto = "P001", cantidad = 10, tipo = "ENTRADA", fecha = 1L))
        dao.insertarMovimiento(Movimiento(codigoProducto = "P001", cantidad = 3, tipo = "SALIDA", fecha = 2L))
        assertEquals(7, dao.existencia("P001"))
    }

    @Test fun actualizaYElimina() = runBlocking {
        dao.insertarProducto(Producto("P002", "Tarima"))
        val id = dao.insertarMovimiento(Movimiento(codigoProducto = "P002", cantidad = 5, tipo = "ENTRADA", fecha = 1L))
        val m = dao.obtenerPorProducto("P002").first { it.id == id }
        dao.actualizarMovimiento(m.copy(cantidad = 8))
        assertEquals(8, dao.existencia("P002"))
        dao.eliminarMovimiento(m)
        assertEquals(0, dao.existencia("P002"))
    }

    @Test fun marcaSincronizados() = runBlocking {
        dao.insertarProducto(Producto("P003", "Bolsa"))
        val id = dao.insertarMovimiento(Movimiento(codigoProducto = "P003", cantidad = 1, tipo = "ENTRADA", fecha = 1L))
        assertEquals(1, dao.obtenerPendientes().size)
        dao.marcarSincronizados(listOf(id))
        assertEquals(0, dao.obtenerPendientes().size)
    }
}
