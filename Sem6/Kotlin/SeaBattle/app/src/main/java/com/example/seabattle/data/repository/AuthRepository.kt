package com.example.seabattle.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await


class AuthRepository(
    private val auth: FirebaseAuth
) {

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    suspend fun signInWithEmail(email: String, password: String): FirebaseUser {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user ?: throw Exception("User is null after sign in")
    }

    suspend fun signUpWithEmail(email: String, password: String): FirebaseUser {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user ?: throw Exception("User is null after sign up")
    }

    fun signOut() {
        auth.signOut()
    }

    fun addAuthStateListener(listener: (FirebaseUser?) -> Unit) {
        auth.addAuthStateListener { firebaseAuth ->
            listener(firebaseAuth.currentUser)
        }
    }
}