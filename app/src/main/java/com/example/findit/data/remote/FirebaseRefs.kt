package com.example.findit.data.remote

import com.google.firebase.firestore.FirebaseFirestore


object FirebaseRefs {
    val firestore = FirebaseFirestore.getInstance()

    // Noms des collections dans la base de données
    const val USERS_COLLECTION = "users"
    const val ITEMS_COLLECTION = "objets"
}