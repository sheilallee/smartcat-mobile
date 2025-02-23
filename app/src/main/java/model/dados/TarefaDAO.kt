package com.application.smartcat.model.dados

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

class TarefaDAO {
    private val db = FirebaseFirestore.getInstance()

    fun buscar(callback: (List<Tarefa>) -> Unit) {
        db.collection("tarefas").get()
            .addOnSuccessListener { querySnapshot ->
                val tarefas = querySnapshot.documents.map { doc ->
                    val dataTimestamp = doc.get("data") as? Timestamp
                    Tarefa(
                        id = doc.id,
                        titulo = doc.getString("titulo") ?: "",
                        descricao = doc.getString("descricao") ?: "",
                        data = dataTimestamp
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
                        data = dataTimestamp
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
        db.collection("tarefas").add(tarefa)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun alterar(id: String, tarefa: Tarefa, callback: (Boolean) -> Unit) {
        db.collection("tarefas").document(id).set(tarefa)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun remover(id: String, callback: (Boolean) -> Unit) {
        db.collection("tarefas").document(id).delete()
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }
}
