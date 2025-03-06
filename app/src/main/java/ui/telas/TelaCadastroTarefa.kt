package com.application.smartcat.ui.telas

import android.app.DatePickerDialog
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.application.smartcat.model.dados.Tarefa
import com.application.smartcat.model.dados.TarefaDAO
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
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
        mutableStateOf<LocalDate?>(
            tarefaParaEditar?.data?.let {
                val instant = Instant.fromEpochSeconds(it.seconds, it.nanoseconds)
                val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                LocalDate.of(dateTime.year, dateTime.monthNumber, dateTime.dayOfMonth)
            }
        )
    }

    var mensagemErro by remember { mutableStateOf<String?>(null) }
    val tarefaDAO = TarefaDAO()

    val hoje = LocalDate.now()
    val limiteMaximo = hoje.plusYears(1)

    // Configuração do DatePickerDialog
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)

            if (selectedDate.isBefore(hoje)) {
                mensagemErro = "Não é permitido selecionar datas no passado."
            } else if (selectedDate.isAfter(limiteMaximo)) {
                mensagemErro = "Selecione uma data até 1 ano no futuro."
            } else {
                dataSelecionada = selectedDate
                mensagemErro = null
            }
        },
        hoje.year,
        hoje.monthValue - 1,
        hoje.dayOfMonth
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
                text = dataSelecionada?.let {
                    "${it.dayOfMonth.toString().padStart(2, '0')}/${it.monthValue.toString().padStart(2, '0')}/${it.year}"
                } ?: "Selecionar Data"
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    if (titulo.isNotEmpty() && descricao.isNotEmpty() && dataSelecionada != null) {
                        val selectedDate = dataSelecionada!!

                        // Validação antes de enviar para o Firebase
                        if (selectedDate.isBefore(hoje)) {
                            mensagemErro = "A data selecionada não pode estar no passado."
                        } else if (selectedDate.isAfter(limiteMaximo)) {
                            mensagemErro = "A data selecionada não pode ultrapassar 1 ano no futuro."
                        } else {
                            // Converte LocalDate para Instant no início do dia selecionado
                            val zone = TimeZone.currentSystemDefault()
                            val instant = LocalDateTime(
                                selectedDate.year,
                                selectedDate.monthValue,
                                selectedDate.dayOfMonth,
                                0, 0
                            ).toInstant(zone)

                            val timestamp = Timestamp(instant.epochSeconds, 0)

                            val tarefa = Tarefa(
                                id = tarefaParaEditar?.id ?: "",
                                titulo = titulo,
                                descricao = descricao,
                                data = timestamp,
                                usuarioId = "" // Definido pelo DAO
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
                        mensagemErro = "Preencha todos os campos."
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (tarefaParaEditar == null) "Adicionar" else "Salvar")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { onCancelar() },
                modifier = Modifier.weight(1f)
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