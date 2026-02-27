package com.example.watchit_movieapp.utilities

import android.util.Log
import com.example.watchit_movieapp.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.firestore

object FireStoreManager {
    val db = Firebase.firestore


    fun checkUserSaved(currentUser: FirebaseUser) {
        val  userRef = db.collection(Constants.FIRESTORE.USERS_REF).document(currentUser.uid)
        userRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                createUser(currentUser)
            }else {
                Log.d(Constants.logMessage.FIRESTORE_KEY, "User already exists in DB")
            }
        }
    }

    fun createUser(currentUser: FirebaseUser) {

        val newUser = User(currentUser.uid, currentUser.displayName ?: "", currentUser.email ?: "")

        db.collection(Constants.FIRESTORE.USERS_REF)
            .document(currentUser.uid).set(newUser).addOnSuccessListener {
                Log.d(Constants.logMessage.FIRESTORE_KEY, "User profile created")
            }.addOnFailureListener {
                Log.d(Constants.logMessage.FIRESTORE_KEY, "error saving user")
            }
    }
}