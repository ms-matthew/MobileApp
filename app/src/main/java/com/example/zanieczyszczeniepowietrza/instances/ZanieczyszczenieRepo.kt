package com.example.zanieczyszczeniepowietrza.instances

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ZanieczyszczenieRepo {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("zanieczyszczenie")

    suspend fun getZanieczyszczenie(): List<Zanieczyszczenie> {
        val snapshot = collection.get().await()
        return snapshot.documents.mapNotNull { it.toObject(Zanieczyszczenie::class.java) }
    }

}