package com.example.seabattle.data.repository

import com.example.seabattle.data.entity.user.ProfileUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firestore: FirebaseFirestore
) {
    suspend fun updateNickname(uid: String, newNickname: String) {
        firestore.collection("users").document(uid)
            .update("nickname", newNickname)
            .await()
    }

    suspend fun updateAvatarName(uid: String, avatarName: String) {
        firestore.collection("users").document(uid)
            .update("avatarName", avatarName)
            .await()
    }

    suspend fun createUserProfile(profile: ProfileUser) {
        firestore.collection("users").document(profile.uid).set(profile).await()
    }

    suspend fun getUserProfile(uid: String): ProfileUser? {
        val snapshot = firestore.collection("users").document(uid).get().await()
        return snapshot.toObject(ProfileUser::class.java)
    }
}