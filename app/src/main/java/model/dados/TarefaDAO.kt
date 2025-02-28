package com.application.smartcat.model.dados

import com.application.smartcat.util.Sessao
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

class TarefaDAO {
    private val db = FirebaseFirestore.getInstance()

    fun buscar(callback: (List<Tarefa>) -> Unit) {
        val usuarioAtual = Sessao.usuarioAtual
        // Verifica se o usuário está logado
        if (usuarioAtual == null) {
            callback(emptyList())
            return
        }
        // Busca apenas as tarefas que possuem o usuárioId do usuário logado
        db.collection("tarefas")
            .whereEqualTo("usuarioId", usuarioAtual.id)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val tarefas = querySnapshot.documents.map { doc ->
                    val dataTimestamp = doc.get("data") as? Timestamp
                    Tarefa(
                        id = doc.id,
                        titulo = doc.getString("titulo") ?: "",
                        descricao = doc.getString("descricao") ?: "",
                        data = dataTimestamp,
                        usuarioId = doc.getString("usuarioId") ?: ""
                    )
                }
                callback(tarefas)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    fun buscarPorId(id: String, callback: (Tarefa?) -> Unit) {
        db.collection("tarefas").document(id).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val dataTimestamp = doc.get("data") as? Timestamp
                    val tarefa = Tarefa(
                        id = doc.id,
                        titulo = doc.getString("titulo") ?: "",
                        descricao = doc.getString("descricao") ?: "",
                        data = dataTimestamp,
                        usuarioId = doc.getString("usuarioId") ?: ""
                    )
                    callback(tarefa)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun adicionar(tarefa: Tarefa, callback: (Boolean) -> Unit) {
        val usuarioAtual = Sessao.usuarioAtual
        // Verifica se o usuário está logado
        if (usuarioAtual == null) {
            callback(false)
            return
        }
        // Adiciona o id do usuario ao objeto tarefa antes de adicioná-lo ao Firestore
        val tarefaComUsuario = tarefa.copy(usuarioId = usuarioAtual.id)
        db.collection("tarefas").add(tarefaComUsuario)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun alterar(id: String, tarefa: Tarefa, callback: (Boolean) -> Unit) {
        // Verifica se o usuário está logado
        val usuarioAtual = Sessao.usuarioAtual
        if (usuarioAtual == null) {
            // Se o usuário não estiver logado, não é possível alterar a tarefa
            callback(false)
            return
        }

        // Adiciona o id do usuario ao objeto tarefa antes de adicioná-lo ao Firestore
        val tarefaComUsuario = tarefa.copy(usuarioId = usuarioAtual.id)
        db.collection("tarefas").document(id).set(tarefaComUsuario)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun remover(id: String, callback: (Boolean) -> Unit) {
        db.collection("tarefas").document(id).delete()
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }
}