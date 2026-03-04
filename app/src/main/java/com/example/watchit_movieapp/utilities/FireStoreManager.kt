package com.example.watchit_movieapp.utilities

import android.util.Log
import com.example.watchit_movieapp.model.MediaItem
import com.example.watchit_movieapp.model.User
import com.example.watchit_movieapp.model.Watchlist
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.UUID

object FireStoreManager {
    val db: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    private val uid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid.toString()


    fun checkUserSaved(currentUser: FirebaseUser) {
        val userRef = db.collection(Constants.FIRESTORE.USERS_REF).document(currentUser.uid)
        userRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                createUser(currentUser)
            } else {
                Log.d(Constants.logMessage.FIRESTORE_KEY, "User already exists in DB")
            }
        }
    }

    fun createUser(currentUser: FirebaseUser) {

        val newUser = User(currentUser.uid, currentUser.displayName ?: "", currentUser.email ?: "")
        val userRef = db.collection(Constants.FIRESTORE.USERS_REF).document(currentUser.uid)

        userRef.set(newUser).addOnSuccessListener {
            Log.d(Constants.logMessage.FIRESTORE_KEY, "User profile created")
        }.addOnFailureListener {
            Log.d(Constants.logMessage.FIRESTORE_KEY, "error saving user")
        }
    }

    fun loadCurrentUser(onUpdate: (User) -> Unit): ListenerRegistration {
        return db.collection(Constants.FIRESTORE.USERS_REF).document(uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.d(Constants.logMessage.FIRESTORE_KEY, "Error loading user", e)
                } else {
                    snapshot?.toObject(User::class.java)?.let { onUpdate(it) }
                }

            }
    }

    fun addTitle(title: MediaItem, listId: String, onResult: (Boolean) -> Unit) {
        val listRef = db.collection(Constants.FIRESTORE.USERS_REF).document(uid)
            .collection(Constants.FIRESTORE.WATCHLISTS_REF).document(listId)

        listRef.collection(Constants.FIRESTORE.TITLES_REF).document(title.id).set(title)
            .addOnSuccessListener {
                Log.d(Constants.logMessage.FIRESTORE_KEY, "added to list")
                onResult(true)
                listRef.update("titleCount", FieldValue.increment(1))
            }.addOnFailureListener {
            Log.d(Constants.logMessage.FIRESTORE_KEY, "adding failed", it)
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
                Log.e(Constants.logMessage.FIRESTORE_KEY, "Error loading list: ${listId}")
                onComplete(emptyList())
            }

    }

    fun removeTitle(titleId: String, listId: String, onResult: (Boolean) -> Unit) {
        val listRef = db.document(
            "${Constants.FIRESTORE.USERS_REF}/${uid}" +
                    "/${Constants.FIRESTORE.WATCHLISTS_REF}/${listId}"
        )

        listRef.collection(Constants.FIRESTORE.TITLES_REF).document("${titleId}")
            .delete().addOnSuccessListener {
                onResult(true)
                listRef.update("titleCount", FieldValue.increment(-1))
            }.addOnFailureListener {
                onResult(false)
            }

    }

    fun addWatchList(listName: String, onResult: (Boolean) -> Unit) {
        val watchlistRef = db.collection(Constants.FIRESTORE.USERS_REF).document(uid)
            .collection(Constants.FIRESTORE.WATCHLISTS_REF).document(UUID.randomUUID().toString())

        val newList = Watchlist(
            id = watchlistRef.id,
            listName = listName
        )

        watchlistRef.set(newList)
            .addOnSuccessListener {
                Log.d(Constants.logMessage.FIRESTORE_KEY, "new list added")
                onResult(true)
            }

    }

    fun deleteWatchlist(listId: String, onResult: (Boolean) -> Unit) {
        val listRef =db.collection(Constants.FIRESTORE.USERS_REF).document(uid)
            .collection(Constants.FIRESTORE.WATCHLISTS_REF).document(listId)

        listRef.collection(Constants.FIRESTORE.TITLES_REF).get()
            .addOnSuccessListener { snapshots ->
                val  batch = db.batch()

                for (document in snapshots.documents) {
                    batch.delete(document.reference)
                }

                batch.delete(listRef)

                batch.commit().addOnSuccessListener {
                    Log.d(Constants.logMessage.FIRESTORE_KEY, "List and all its movies deleted successfully")
                    onResult(true)
                }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error deleting list: ${e.message}")
                        onResult(false)
                    }
            }
            .addOnFailureListener { onResult(false) }



    }

    fun showMyLists(onUpdate: (List<Watchlist>) -> Unit): ListenerRegistration? {
        val watchlistRef = db.collection(Constants.FIRESTORE.USERS_REF).document(uid)
            .collection(Constants.FIRESTORE.WATCHLISTS_REF)

        return watchlistRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.d(Constants.logMessage.FIRESTORE_KEY, "Error observing watchLists", e)

            } else {
                if (snapshot != null) {
                    val watchLists = snapshot.toObjects(Watchlist::class.java)
                    onUpdate(watchLists)
                }
            }


        }

    }

    fun getFriendWatchlists(friendId: String, onComplete: (List<Watchlist>) -> Unit) {
        db.collection(Constants.FIRESTORE.USERS_REF).document(friendId)
            .collection(Constants.FIRESTORE.WATCHLISTS_REF)
            .get() // קריאה חד פעמית
            .addOnSuccessListener { snapshot ->
                val watchlists = snapshot.toObjects(Watchlist::class.java)
                onComplete(watchlists)
            }
            .addOnFailureListener {
                onComplete(emptyList())
            }
    }

    fun addFriend(friendId: String, onResult: (Boolean) -> Unit) {
        val myRef = db.collection(Constants.FIRESTORE.USERS_REF).document(uid)
        val friendRef = db.collection(Constants.FIRESTORE.USERS_REF).document(friendId)
        val batch = db.batch()

        batch.update(myRef, "friendsList", FieldValue.arrayUnion(friendId))
        batch.update(friendRef, "friendsList", FieldValue.arrayUnion(uid))

        batch.commit()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun removeFriend(friendId: String, onResult: (Boolean) -> Unit) {

        val myRef = db.collection(Constants.FIRESTORE.USERS_REF).document(uid)
        val friendRef = db.collection(Constants.FIRESTORE.USERS_REF).document(friendId)
        val batch = db.batch()

        batch.update(myRef, "friendsList", FieldValue.arrayRemove(friendId))
        batch.update(friendRef, "friendsList", FieldValue.arrayRemove(uid))

        batch.commit()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }


    }

    fun getFriendsList(friends: List<String>, onComplete: (List<User>) -> Unit) {
        if (friends.isEmpty()) {
            onComplete(emptyList())
            return
        }
        db.collection(Constants.FIRESTORE.USERS_REF)
            .whereIn("uid", friends)
            .get().addOnSuccessListener { snapshot ->
                val friends = snapshot.toObjects(User::class.java)
                onComplete(friends)
            }.addOnFailureListener {
                Log.e(Constants.logMessage.FIRESTORE_KEY, "Error fetching friends", it)
                onComplete(listOf())
            }

    }

    fun getFriendProfile(userid: String, onComplete: (User?) -> Unit) {
        db.collection(Constants.FIRESTORE.USERS_REF).document(userid)
            .get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.toObject(User::class.java)
                onComplete(user)
            }
            .addOnFailureListener {
                Log.e(Constants.logMessage.FIRESTORE_KEY, "Error getting friend details", it)
                onComplete(null)
            }
    }

    fun addFavorite(title: MediaItem, onResult: (Boolean) -> Unit) {
        val userRef = db.collection(Constants.FIRESTORE.USERS_REF).document(uid)
        val favRef = userRef.collection(Constants.FIRESTORE.FAVORITES)

        favRef.document(title.id).set(title).addOnSuccessListener {
            Log.d(Constants.logMessage.FIRESTORE_KEY, "added to Favorites")
            userRef.update("favorites", FieldValue.arrayUnion(title.id))
            onResult(true)

        }.addOnFailureListener {
            Log.d(Constants.logMessage.FIRESTORE_KEY, "adding failed")
            onResult(false)
        }

    }

    fun deleteFavorite(titleId: String, onResult: (Boolean) -> Unit) {
        val userRef = db.collection(Constants.FIRESTORE.USERS_REF).document(uid)
        val favRef = userRef.collection(Constants.FIRESTORE.FAVORITES)

        favRef.document(titleId).delete().addOnSuccessListener {
            Log.d(Constants.logMessage.FIRESTORE_KEY, "deleted from Favorites")
            userRef.update("favorites", FieldValue.arrayRemove(titleId))
            onResult(true)

        }.addOnFailureListener {
            Log.d(Constants.logMessage.FIRESTORE_KEY, "delete failed")
            onResult(false)
        }
    }

    fun showFavorites(userId: String, onComplete: (List<MediaItem>) -> Unit) {
        db.collection(Constants.FIRESTORE.USERS_REF).document(userId)
            .collection(Constants.FIRESTORE.FAVORITES)
            .get()
            .addOnSuccessListener { snapshot ->
                val movies = snapshot.toObjects(MediaItem::class.java)
                onComplete(movies)
            }.addOnFailureListener {
                onComplete(emptyList())
            }

    }

    fun saveUserRating(titleId: String, rating: Float, onComplete: (Boolean) -> Unit) {

        val data = mapOf(
            "titleId" to titleId,
            "myRating" to rating
        )

        db.collection(Constants.FIRESTORE.USERS_REF).document(uid)
            .collection(Constants.FIRESTORE.RATINGS).document(titleId) // ה-ID של הסרט הוא שם המסמך
            .set(data)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun getUserRating(titleId: String, onResult: (Float?) -> Unit) {
        val ratingRef = db.collection(Constants.FIRESTORE.USERS_REF).document(uid)
            .collection(Constants.FIRESTORE.RATINGS)

        ratingRef.document(titleId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val rating = document.getDouble("myRating")?.toFloat()
                    onResult(rating)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

}