package com.application.smartcat.ui.telas

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.application.smartcat.model.dados.Tarefa
import com.application.smartcat.model.dados.TarefaDAO
import com.application.smartcat.util.formatInstant
import com.application.smartcat.util.parseDate
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.Instant

@Composable
fun TelaCadastroTarefa(
    modifier: Modifier = Modifier,
    tarefaParaEditar: Tarefa? = null,
    onCadastroSucesso: () -> Unit,
    onCancelar: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var titulo by remember { mutableStateOf(tarefaParaEditar?.titulo ?: "") }
    var descricao by remember { mutableStateOf(tarefaParaEditar?.descricao ?: "") }
    var dataSelecionada by remember {
        mutableStateOf(
            tarefaParaEditar?.data?.let {
                val instant = Instant.fromEpochSeconds(it.seconds, it.nanoseconds)
                formatInstant(instant)
            } ?: ""
        )
    }
    var mensagemErro by remember { mutableStateOf<String?>(null) }
    val tarefaDAO = TarefaDAO()

    val now = Clock.System.now()
    val localNow = now.toLocalDateTime(TimeZone.currentSystemDefault())
    val currentYear = localNow.year
    val currentMonth = localNow.monthNumber - 1
    val currentDay = localNow.dayOfMonth

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val dayStr = dayOfMonth.toString().padStart(2, '0')
            val monthStr = (month + 1).toString().padStart(2, '0')
            dataSelecionada = "$dayStr/$monthStr/$year"
        },
        currentYear,
        currentMonth,
        currentDay
    )

    Column(
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        modifier = modifier.padding(16.dp)
    ) {

        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Título") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = descricao,
            onValueChange = { descricao = it },
            label = { Text("Descrição") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { datePickerDialog.show() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (dataSelecionada.isNotEmpty()) "Data: $dataSelecionada"
                else "Selecionar Data"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Selecionar data"
            )
        }
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Button(
                onClick = {
                    if (titulo.isNotEmpty() && descricao.isNotEmpty() && dataSelecionada.isNotEmpty()) {
                        val instantData = parseDate(dataSelecionada)
                        if (instantData == null) {
                            mensagemErro = "Data inválida. Use o formato dd/MM/yyyy."
                        } else {
                            val timestampData = Timestamp(instantData.epochSeconds, 0)
                            val tarefa = Tarefa(
                                id = tarefaParaEditar?.id ?: "",
                                titulo = titulo,
                                descricao = descricao,
                                data = timestampData,
                                usuarioId = "" // Será definido no DAO automaticamente
                            )
                            scope.launch(Dispatchers.IO) {
                                if (tarefaParaEditar == null) {
                                    tarefaDAO.adicionar(tarefa) { sucesso ->
                                        scope.launch(Dispatchers.Main) {
                                            if (sucesso) onCadastroSucesso()
                                            else mensagemErro = "Falha ao adicionar tarefa."
                                        }
                                    }
                                } else {
                                    tarefaDAO.alterar(tarefaParaEditar.id, tarefa) { sucesso ->
                                        scope.launch(Dispatchers.Main) {
                                            if (sucesso) onCadastroSucesso()
                                            else mensagemErro = "Falha ao atualizar tarefa."
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        mensagemErro = "Todos os campos são obrigatórios."
                    }
                },
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(text = if (tarefaParaEditar == null) "Adicionar" else "Salvar")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { onCancelar() },
                modifier = Modifier
                    .weight(1f) // Ocupa a outra metade da largura
            ) {
                Text("Cancelar")
            }
        }

        mensagemErro?.let { erro ->
            LaunchedEffect(erro) {
                Toast.makeText(context, erro, Toast.LENGTH_SHORT).show()
                mensagemErro = null
            }
        }
    }
}