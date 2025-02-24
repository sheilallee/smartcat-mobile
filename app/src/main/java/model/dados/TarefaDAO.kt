package com.application.smartcat.model.dados

import com.application.smartcat.util.Sessao
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

class TarefaDAO {
    private val db = FirebaseFirestore.getInstance()

    fun buscar(callback: (List<Tarefa>) -> Unit) {
        val currentUser = Sessao.usuarioAtual
        if (currentUser == null) {
            callback(emptyList())
            return
        }
        // Busca apenas as tarefas que possuem o usuárioId do usuário logado
        db.collection("tarefas")
            .whereEqualTo("usuarioId", currentUser.id)
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
        val currentUser = Sessao.usuarioAtual
        if (currentUser == null) {
            callback(false)
            return
        }
        // Garante que a tarefa receba o ID do usuário logado, sem que o usuário veja esse campo
        val tarefaComUsuario = tarefa.copy(usuarioId = currentUser.id)
        db.collection("tarefas").add(tarefaComUsuario)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun alterar(id: String, tarefa: Tarefa, callback: (Boolean) -> Unit) {
        val currentUser = Sessao.usuarioAtual
        if (currentUser == null) {
            callback(false)
            return
        }
        val tarefaComUsuario = tarefa.copy(usuarioId = currentUser.id)
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




