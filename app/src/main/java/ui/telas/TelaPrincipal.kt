package com.application.smartcat.ui.telas

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.application.smartcat.model.dados.Tarefa
import com.application.smartcat.model.dados.TarefaDAO
import com.application.smartcat.util.formatInstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

@Composable
fun TelaPrincipal(
    modifier: Modifier = Modifier,
    onLogoffClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tarefaDAO = TarefaDAO()
    var tarefas by remember { mutableStateOf<List<Tarefa>>(emptyList()) }
    var mensagemErro by remember { mutableStateOf<String?>(null) }
    var showCadastroModal by remember { mutableStateOf(false) }
    var tarefaSelecionada by remember { mutableStateOf<Tarefa?>(null) }

    LaunchedEffect(Unit) {
        tarefaDAO.buscar { lista ->
            tarefas = lista
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF8E8CCC))
            .padding(16.dp)
    ) {
        Text(
            text = "Bem-vindo(a) de volta!\n\nSuas próximas tarefas:",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(tarefas) { tarefa ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD0CFEA)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Título: ${tarefa.titulo}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Descrição: ${tarefa.descricao}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Data: " + if (tarefa.data != null) {
                                val instant = kotlinx.datetime.Instant.fromEpochSeconds(tarefa.data.seconds, tarefa.data.nanoseconds)
                                formatInstant(instant)
                            } else "Sem data",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = {
                                tarefaSelecionada = tarefa
                                showCadastroModal = true
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar tarefa"
                                )
                            }
                            IconButton(onClick = {
                                scope.launch(Dispatchers.IO) {
                                    tarefaDAO.remover(tarefa.id) { sucesso ->
                                        scope.launch(Dispatchers.Main) {
                                            if (sucesso) {
                                                tarefas = tarefas.filter { it.id != tarefa.id }
                                            } else {
                                                Toast.makeText(context, "Erro ao remover tarefa", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remover tarefa"
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        FloatingActionButton(
            onClick = {
                tarefaSelecionada = null
                showCadastroModal = true
            },
            containerColor = Color(0xFF8E8CCC),
            contentColor = Color.White,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("+ Adicionar")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onLogoffClick() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sair")
        }
    }

    if (showCadastroModal) {
        AlertDialog(
            onDismissRequest = { showCadastroModal = false },
            title = {
                Text(text = if (tarefaSelecionada == null) "Adicionar Tarefa" else "Alterar Tarefa")
            },
            text = {
                TelaCadastroTarefa(
                    tarefaParaEditar = tarefaSelecionada,
                    onCadastroSucesso = {
                        tarefaDAO.buscar { lista ->
                            tarefas = lista
                        }
                        showCadastroModal = false
                    },
                    onCancelar = { showCadastroModal = false }
                )
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}

