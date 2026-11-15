package com.example.findit.data.repository

import com.example.findit.data.model.LostItem
import com.example.findit.data.remote.FirebaseRefs

class ItemRepository {
    private val itemsCollection = FirebaseRefs.firestore.collection(FirebaseRefs.ITEMS_COLLECTION)

    fun getItems(onResult: (List<LostItem>, String?) -> Unit) {
        itemsCollection.get()
            .addOnSuccessListener { result ->
                val items = mutableListOf<LostItem>()
                for (doc in result) {
                    val item = doc.toObject(LostItem::class.java).copy(id = doc.id)
                    items.add(item)
                }
                onResult(items, null)
            }
            .addOnFailureListener { e -> onResult(emptyList(), e.localizedMessage) }
    }

    fun addItem(item: LostItem, onResult: (Boolean, String?) -> Unit) {
        val docRef = itemsCollection.document()
        val itemWithId = item.copy(id = docRef.id)
        docRef.set(itemWithId)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.localizedMessage) }
    }

    fun getItemDetail(itemId: String, onResult: (LostItem?, String?) -> Unit) {
        itemsCollection.document(itemId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val item = doc.toObject(LostItem::class.java)?.copy(id = doc.id)
                    onResult(item, null)
                } else {
                    onResult(null, "Objet introuvable")
                }
            }
            .addOnFailureListener { e -> onResult(null, e.localizedMessage) }
    }
}
