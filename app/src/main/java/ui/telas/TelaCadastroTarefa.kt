package com.application.smartcat.ui.telas

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.application.smartcat.model.dados.Tarefa
import com.application.smartcat.model.dados.TarefaDAO
import com.application.smartcat.util.Sessao
import com.application.smartcat.util.parseDate
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun TelaCadastroTarefa(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tarefaDAO = TarefaDAO()

    var titulo by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var dataSelecionada by remember { mutableStateOf("") }
    var mensagemErro by remember { mutableStateOf<String?>(null) }

    val now = Clock.System.now()
    val localDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val dayStr = dayOfMonth.toString().padStart(2, '0')
            val monthStr = (month + 1).toString().padStart(2, '0')
            dataSelecionada = "$dayStr/$monthStr/$year"
        },
        localDateTime.year,
        localDateTime.monthNumber - 1,
        localDateTime.dayOfMonth
    )

    // Exibir o modal
    Dialog(onDismissRequest = { navController.popBackStack() }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Adicionar Tarefa",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.width(280.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição") },
                    modifier = Modifier.width(280.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.width(280.dp)
                ) {
                    Text(if (dataSelecionada.isNotEmpty()) "Data: $dataSelecionada" else "Selecionar Data")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.Default.DateRange, contentDescription = "Selecionar data")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = {
                        if (titulo.isNotEmpty() && descricao.isNotEmpty() && dataSelecionada.isNotEmpty()) {
                            val instantData = parseDate(dataSelecionada)
                            if (instantData == null) {
                                mensagemErro = "Formato de data inválido."
                            } else {
                                val dataAtual = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
                                val dataSelecionadaLocal = instantData.toLocalDateTime(TimeZone.currentSystemDefault()).date

                                if (dataSelecionadaLocal < dataAtual) {
                                    mensagemErro = "A data não pode ser menor que a atual."
                                } else {
                                    val timestampData = Timestamp(instantData.epochSeconds, 0)
                                    val usuarioAtual = Sessao.usuarioAtual
                                    if (usuarioAtual == null) {
                                        mensagemErro = "Erro: Usuário não autenticado."
                                        return@Button
                                    }

                                    val tarefa = Tarefa(
                                        titulo = titulo,
                                        descricao = descricao,
                                        data = timestampData,
                                        usuarioId = usuarioAtual.id,
                                        status = 1 // Sempre começa como "A Fazer"
                                    )

                                    scope.launch(Dispatchers.IO) {
                                        tarefaDAO.adicionar(tarefa) { sucesso ->
                                            scope.launch(Dispatchers.Main) {
                                                if (sucesso) navController.popBackStack()
                                                else mensagemErro = "Erro ao adicionar."
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            mensagemErro = "Todos os campos são obrigatórios."
                        }
                    }) {
                        Text("Adicionar")
                    }

                    Button(onClick = { navController.popBackStack() }) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }

    mensagemErro?.let {
        LaunchedEffect(it) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            mensagemErro = null
        }
    }
}



