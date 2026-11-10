package com.example.findit.data.model

data class LostItem(
    val id: String = "",
    val titre: String = "",
    val description: String = "",
    val photoUrl: String = "",
    val lieu: String = "",
    val date: Long = System.currentTimeMillis(),
    val statut: String = "PERDU", // PERDU, TROUVE, RESTITUE
    val userId: String = "" // L'ID de la personne qui a déclaré l'objet
)