package com.example.watchit_movieapp.utilities

import android.util.Log
import com.example.watchit_movieapp.model.MediaItem
import com.example.watchit_movieapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

object FireStoreManager {
    val db: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    private val uid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid.toString()



    fun checkUserSaved(currentUser : FirebaseUser) {
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
        val  userRef =   db.collection(Constants.FIRESTORE.USERS_REF).document(currentUser.uid)

       userRef.set(newUser).addOnSuccessListener {
                Log.d(Constants.logMessage.FIRESTORE_KEY, "User profile created")
                userRef.collection(Constants.FIRESTORE.FAVORITES)
            }.addOnFailureListener {
                Log.d(Constants.logMessage.FIRESTORE_KEY, "error saving user")
            }
    }

    fun addTitle(title: MediaItem, listName: String,onResult: (Boolean) -> Unit){
        val listRef = db.collection(Constants.FIRESTORE.USERS_REF).document(uid)
            .collection(Constants.FIRESTORE.WATCHLISTS_REF).document(listName)

        listRef.collection(Constants.FIRESTORE.TITLES_REF).
        document(title.id).set(title).addOnSuccessListener {
            Log.d(Constants.logMessage.FIRESTORE_KEY, "added to list")
            onResult(true)
        }.addOnFailureListener {
            Log.d(Constants.logMessage.FIRESTORE_KEY, "adding failed")
            onResult(false)
        }

    }

    fun loadWatchlist(userid: String, listId: String, onComplete: (List<MediaItem>) -> Unit) {

        db.collection(Constants.FIRESTORE.USERS_REF).document(userid)
            .collection(Constants.FIRESTORE.WATCHLISTS_REF).document(listId)
            .collection(Constants.FIRESTORE.TITLES_REF)
            .get()
            .addOnSuccessListener { docs ->
                val movieList = docs.toObjects(MediaItem::class.java)
                onComplete(movieList)
            }
            .addOnFailureListener {
                Log.e(Constants.logMessage.FIRESTORE_KEY, "Error loading list: $listId", it)
            }
    }

    fun removeTitle(titleId: String, listId: String, onResult: (Boolean) -> Unit){
        db.document("${Constants.FIRESTORE.USERS_REF}/${uid}" +
                "/${Constants.FIRESTORE.WATCHLISTS_REF}/${listId}/${Constants.FIRESTORE.TITLES_REF}" +
                "${titleId}").delete().addOnSuccessListener {
                    onResult(true)
                }.addOnFailureListener {
            onResult(false)
        }

    }

    fun addWatchList(listId: String, onResult: (Boolean) -> Unit){
        val watchlistRef = db.collection(Constants.FIRESTORE.USERS_REF).document(uid)
            .collection(Constants.FIRESTORE.WATCHLISTS_REF)


    }

    fun  deleteWatchlist(listId: String, onResult: (Boolean) -> Unit){

    }

    fun addFriend(userid: String , onResult: (Boolean) -> Unit ){

    }

    fun removeFriend(userid:String){


    }

    fun loadWatchLists(){

    }






}