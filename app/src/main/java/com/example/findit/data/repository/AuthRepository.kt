package com.example.findit.data.repository

import com.example.findit.data.remote.FirebaseRefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseRefs.firestore

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun signIn(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.localizedMessage) }
    }

    /**
     * Legacy/backward-compatible signUp method.
     */
    fun signUp(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        signUp(
            firstName = "",
            lastName = "",
            email = email,
            password = password,
            role = "etudiant",
            onResult = onResult
        )
    }

    /**
     * Complete signUp with user profile creation in Cloud Firestore ("users" collection).
     */
    fun signUp(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        role: String = "etudiant",
        onResult: (Boolean, String?) -> Unit
    ) {
        val cleanEmail = email.trim()
        auth.createUserWithEmailAndPassword(cleanEmail, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user == null) {
                    onResult(false, "User creation failed: Firebase user is null.")
                    return@addOnSuccessListener
                }

                val uid = user.uid
                val userRole = role.ifBlank { "etudiant" }
                val fullName = "${firstName.trim()} ${lastName.trim()}".trim()

                val userDocRef = firestore.collection(FirebaseRefs.USERS_COLLECTION).document(uid)

                // Check if profile document already exists before writing
                userDocRef.get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            // Document already exists; preserve all existing profile data safely without overwriting
                            onResult(true, null)
                        } else {
                            val userData = hashMapOf(
                                "uid" to uid,
                                "firstName" to firstName.trim(),
                                "lastName" to lastName.trim(),
                                "fullName" to fullName,
                                "email" to cleanEmail,
                                "role" to userRole
                            )
                            userDocRef.set(userData)
                                .addOnSuccessListener {
                                    onResult(true, null)
                                }
                                .addOnFailureListener { e ->
                                    val err = e.localizedMessage ?: "Firestore write error"
                                    handleProfileFailure(user, err, onResult)
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        val err = e.localizedMessage ?: "Firestore status check error"
                        handleProfileFailure(user, err, onResult)
                    }
            }
            .addOnFailureListener { e ->
                onResult(false, e.localizedMessage)
            }
    }

    /**
     * Reliable error cleanup strategy when Firestore profile creation fails.
     * Ensures sign-out and handles asynchronous user deletion cleanly.
     */
    private fun handleProfileFailure(
        user: FirebaseUser,
        profileError: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        user.delete().addOnCompleteListener { deleteSnapshot ->
            // Always sign out to prevent leaving an active un-profiled session
            auth.signOut()
            if (deleteSnapshot.isSuccessful) {
                onResult(false, "Profile creation failed: $profileError")
            } else {
                val deleteErr = deleteSnapshot.exception?.localizedMessage ?: "Unknown cleanup error"
                onResult(false, "Profile creation failed: $profileError (Account cleanup error: $deleteErr)")
            }
        }
    }

    fun sendPasswordResetEmail(email: String, onResult: (Boolean, String?) -> Unit) {
        auth.sendPasswordResetEmail(email.trim())
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.localizedMessage) }
    }

    fun signOut() {
        auth.signOut()
    }
}
