package com.example.watchit_movieapp.utilities

import android.net.Uri
import android.util.Log
import com.example.watchit_movieapp.model.MediaItem
import com.example.watchit_movieapp.model.User
import com.example.watchit_movieapp.model.Watchlist
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

object FireStoreManager {
    val db: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    val storage: FirebaseStorage
        get() = FirebaseStorage.getInstance()

    private val uid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid?:""


    var currentUser: User? = null
        private set

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

    fun loadCurrentUser(onUpdate: (User?) -> Unit): ListenerRegistration? {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        return db.collection(Constants.FIRESTORE.USERS_REF).document(currentUid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.d(Constants.logMessage.FIRESTORE_KEY, "Error loading user", e)
                    onUpdate(null)
                } else {
                    val user = snapshot?.toObject(User::class.java)
                    if (user != null) {
                        this.currentUser = user
                        onUpdate(user)
                    }
                }

            }
    }

    fun isInFavorites(titleId: String): Boolean {
        return currentUser?.favorites?.contains(titleId) == true
    }

    fun addTitle(title: MediaItem, listId: String, onComplete: (String) -> Unit) {
        val listRef = db.collection(Constants.FIRESTORE.USERS_REF).document(uid)
            .collection(Constants.FIRESTORE.WATCHLISTS_REF).document(listId)

        listRef.collection(Constants.FIRESTORE.TITLES_REF)
            .document(title.id).get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    onComplete(Constants.logMessage.EXISTS)
                    Log.d(Constants.logMessage.FIRESTORE_KEY, Constants.logMessage.EXISTS)
                } else {
                    listRef.collection(Constants.FIRESTORE.TITLES_REF)
                        .document(title.id).set(title).addOnSuccessListener {
                            Log.d(Constants.logMessage.FIRESTORE_KEY,"added to list")
                            listRef.update("titleCount", FieldValue.increment(1))
                            onComplete(Constants.logMessage.SUCCESS)
                        }.addOnFailureListener {
                            Log.d(Constants.logMessage.FIRESTORE_KEY, "adding failed", it)
                            onComplete(Constants.logMessage.FAIL)
                        }
                }
            }.addOnFailureListener {
                Log.d(Constants.logMessage.FIRESTORE_KEY, "adding failed", it)
                onComplete(Constants.logMessage.FAIL)
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
                Log.e(Constants.logMessage.FIRESTORE_KEY, "Error loading list: $listId")
                onComplete(emptyList())
            }

    }

    fun removeTitle(titleId: String, listId: String, onResult: (Boolean) -> Unit) {
        val listRef = db.document(
            "${Constants.FIRESTORE.USERS_REF}/${uid}" +
                    "/${Constants.FIRESTORE.WATCHLISTS_REF}/${listId}"
        )

        listRef.collection(Constants.FIRESTORE.TITLES_REF).document(titleId)
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
        val listRef = db.collection(Constants.FIRESTORE.USERS_REF).document(uid)
            .collection(Constants.FIRESTORE.WATCHLISTS_REF).document(listId)

        listRef.collection(Constants.FIRESTORE.TITLES_REF).get()
            .addOnSuccessListener { snapshots ->
                val batch = db.batch()

                for (document in snapshots.documents) {
                    batch.delete(document.reference)
                }

                batch.delete(listRef)

                batch.commit().addOnSuccessListener {
                    Log.d(
                        Constants.logMessage.FIRESTORE_KEY,
                        "List and all its movies deleted successfully"
                    )
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
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        val watchlistRef = db.collection(Constants.FIRESTORE.USERS_REF).document(currentUid)
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
            .addOnFailureListener {exception ->
                Log.e(Constants.logMessage.FIRESTORE_KEY, "Error getting friend details", exception)
                onComplete(null)
            }
    }

    fun observeFriendship(friendId: String, onStatusChanged: (Boolean) -> Unit): ListenerRegistration? {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return null

        return db.collection(Constants.FIRESTORE.USERS_REF).document(currentUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(Constants.logMessage.FIRESTORE_KEY, "Error observing friendship", error)
                    return@addSnapshotListener
                }

                val user = snapshot?.toObject(User::class.java)
                val isFriend = user?.friendsList?.contains(friendId) ?: false
                onStatusChanged(isFriend)
            }
    }


    fun searchUsers(searchName: String, callback: (List<User>) -> Unit){
        val usersRef = db.collection(Constants.FIRESTORE.USERS_REF)

        usersRef.orderBy("username").startAt(searchName).
                endAt(searchName+"\uf8ff").get()
            .addOnSuccessListener {docs ->
                val userList = mutableListOf<User>()
                for(doc in docs){
                    val user = doc.toObject(User::class.java)
                    if(user.uid != uid){
                        userList.add(user)
                    }
                }
                Log.d(Constants.logMessage.FIRESTORE_KEY, "got users")
                callback(userList)
            }.addOnFailureListener {exception ->
                Log.d(Constants.logMessage.FIRESTORE_KEY, Constants.logMessage.FAIL,exception)
                callback(emptyList())
            }

    }

    fun addFavorite(title: MediaItem, onResult: (Boolean) -> Unit) {
        Log.d("FirebaseCheck", "Current UID: $uid")
        val userRef = db.collection(Constants.FIRESTORE.USERS_REF).document(uid)
        val favRef = userRef.collection(Constants.FIRESTORE.FAVORITES)

        favRef.document(title.id).set(title).addOnSuccessListener {
            Log.d(Constants.logMessage.FIRESTORE_KEY, "added to Favorites")
            userRef.update("favorites", FieldValue.arrayUnion(title.id))
            onResult(true)

        }.addOnFailureListener {exception ->
            Log.d(Constants.logMessage.FIRESTORE_KEY, "adding failed",exception)
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
            Log.d(Constants.logMessage.FIRESTORE_KEY, "delete failed",it)
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
                    onResult(0f)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun uploadProfileImage(uri: Uri, onComplete: (String?) -> Unit) {
        val storageRef = storage.reference.child("profile_pics/$uid")
        val profileRef = db.collection(Constants.FIRESTORE.USERS_REF).document(uid)

        storageRef.putFile(uri).continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {exception ->
                    Log.d(Constants.logMessage.STORAGE_KEY,"upload failed",exception)
                }
            }
            storageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result.toString()

                profileRef.update("profileImageUrl", downloadUri)
                    .addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            onComplete(downloadUri)
                        } else {
                            Log.d(Constants.logMessage.STORAGE_KEY,"upload failed",task.exception)
                            onComplete(null)
                        }
                    }
            } else {
                onComplete(null)
            }

        }

    }

}