package com.example.stockmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stockmanager.ui.theme.StockManagerTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StockManagerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    StockScreen(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun StockScreen(modifier: Modifier = Modifier, vm: StockViewModel = viewModel()) {
    val lista by vm.movimientos.collectAsState()
    var codigo by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("ENTRADA") }
    var editando by remember { mutableStateOf<Movimiento?>(null) }
    val formato = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    fun limpiar() {
        codigo = ""; cantidad = ""; tipo = "ENTRADA"; editando = null
    }

    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("StockManager", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = codigo, onValueChange = { codigo = it },
            label = { Text("Codigo del producto") },
            enabled = editando == null, singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = cantidad, onValueChange = { cantidad = it.filter { c -> c.isDigit() } },
            label = { Text("Cantidad") }, singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = tipo == "ENTRADA", onClick = { tipo = "ENTRADA" }, label = { Text("ENTRADA") })
            FilterChip(selected = tipo == "SALIDA", onClick = { tipo = "SALIDA" }, label = { Text("SALIDA") })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                vm.guardar(codigo, cantidad, tipo, editando)
                if (editando != null) limpiar()
            }) { Text(if (editando == null) "Guardar" else "Actualizar") }
            if (editando != null) OutlinedButton(onClick = { limpiar() }) { Text("Cancelar") }
            OutlinedButton(onClick = { vm.consultarExistencia(codigo) }) { Text("Ver existencia") }
        }
        OutlinedButton(onClick = { vm.sincronizar() }, modifier = Modifier.fillMaxWidth()) {
            Text("Sincronizar con servidor")
        }

        if (vm.mensaje.isNotEmpty()) Text(vm.mensaje, color = MaterialTheme.colorScheme.primary)
        vm.existencia?.let { Text("Existencia actual: $it", fontWeight = FontWeight.SemiBold) }

        Spacer(Modifier.height(4.dp))
        Text("Movimientos (${lista.size})", style = MaterialTheme.typography.titleMedium)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(lista, key = { it.id }) { m ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("${m.codigoProducto} - ${m.tipo}", fontWeight = FontWeight.Bold)
                            Text("Cantidad: ${m.cantidad}")
                            Text(formato.format(Date(m.fecha)), style = MaterialTheme.typography.bodySmall)
                            Text(
                                if (m.sincronizado) "Sincronizado" else "Pendiente",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        TextButton(onClick = {
                            editando = m; codigo = m.codigoProducto
                            cantidad = m.cantidad.toString(); tipo = m.tipo
                        }) { Text("Editar") }
                        TextButton(onClick = { vm.eliminar(m) }) { Text("Borrar") }
                    }
                }
            }
        }
    }
}
