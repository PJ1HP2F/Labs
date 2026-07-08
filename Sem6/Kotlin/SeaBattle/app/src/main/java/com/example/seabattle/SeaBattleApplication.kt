package com.example.seabattle

import android.app.Application
import com.example.seabattle.data.repository.AuthRepository
import com.example.seabattle.data.repository.GameRepository
import com.example.seabattle.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SeaBattleApplication: Application() {

    private val firebaseFirestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    val authRepository by lazy {
        AuthRepository(
            auth = firebaseAuth
        )
    }

    val userRepository by lazy {
        UserRepository(
            firestore = firebaseFirestore
        )
    }

    val gameRepository by lazy {
        GameRepository(
            firestore = firebaseFirestore
        )
    }
}