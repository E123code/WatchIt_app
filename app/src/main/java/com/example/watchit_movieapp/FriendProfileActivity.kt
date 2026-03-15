package com.example.watchit_movieapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.watchit_movieapp.adapters.UserAdapter
import com.example.watchit_movieapp.adapters.WatchlistAdapter
import com.example.watchit_movieapp.databinding.ActivityFriendProfieBinding
import com.example.watchit_movieapp.interfaces.ListClickedCallback
import com.example.watchit_movieapp.interfaces.UserClickedCallback
import com.example.watchit_movieapp.model.User
import com.example.watchit_movieapp.model.Watchlist
import com.example.watchit_movieapp.utilities.AdapterMode
import com.example.watchit_movieapp.utilities.Constants
import com.example.watchit_movieapp.utilities.FireStoreManager
import com.example.watchit_movieapp.utilities.ImageLoader
import com.example.watchit_movieapp.utilities.SignalManager
import com.google.firebase.firestore.ListenerRegistration

class FriendProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFriendProfieBinding
    private var friendshipListener: ListenerRegistration? = null

    private lateinit var friendsAdapter: UserAdapter
    private lateinit var watchlistAdapter: WatchlistAdapter

    private var friendId: String? = null
    private var isFriend: Boolean = false

    private var currentFriendUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendProfieBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bundle: Bundle? = intent.extras
        friendId = bundle?.getString(Constants.bundlekeys.ID_KEY)

        if (friendId == null) {
            finish()
            return
        }

        setupRecycleView()
        initViews()

    }

    private fun setupRecycleView() {
        val friendCallback = object : UserClickedCallback {
            override fun userClicked(userId: String) {
                val intent = Intent(this@FriendProfileActivity, FriendProfileActivity::class.java)
                var bundle = Bundle()
                bundle.putString(Constants.bundlekeys.ID_KEY, userId)

                intent.putExtras(bundle)
                startActivity(intent)
            }
        }

        friendsAdapter = UserAdapter(emptyList(), friendCallback)

        binding.RVFriends.adapter = friendsAdapter
        binding.RVFriends.layoutManager = LinearLayoutManager(this)

        val listCallback = object : ListClickedCallback {
            override fun watchlistClicked(watchlist: Watchlist) {
                val intent = Intent(this@FriendProfileActivity, WatchlistActivity::class.java)
                var bundle = Bundle()
                bundle.putString(Constants.bundlekeys.ID_KEY, friendId)
                if (watchlist.id == Constants.FIRESTORE.FAVORITES) {
                    bundle.putString(
                        Constants.bundlekeys.LIST_ID_KEY,
                        Constants.FIRESTORE.FAVORITES
                    )
                    bundle.putString(Constants.bundlekeys.LIST_NAME_KEY, "Favorites")
                } else {
                    bundle.putString(Constants.bundlekeys.LIST_ID_KEY, watchlist.id)
                    bundle.putString(Constants.bundlekeys.LIST_NAME_KEY, watchlist.listName)
                }
                intent.putExtras(bundle)
                startActivity(intent)
            }

            override fun deleteListClicked(watchlist: Watchlist) {


            }
        }

        watchlistAdapter =
            WatchlistAdapter(emptyList(), mode = AdapterMode.FRIEND_MODE, callback = listCallback)

        binding.RVWatchlists.adapter = watchlistAdapter
        binding.RVWatchlists.layoutManager = LinearLayoutManager(this)

    }

    private fun initViews() {

        binding.BTNBack.setOnClickListener { finish() }

        val myId = FireStoreManager.getInstance().currentUser?.uid?:""
        if (friendId == myId){
            binding.BTNFriendAction.visibility = View.GONE
        }

        binding.BTNFriendAction.setOnClickListener {
            val id = friendId
            if (id != null) {
                if (isFriend) {
                    FireStoreManager.getInstance().removeFriend(id) {
                        SignalManager.getInstance()
                            .toast("Friend removed", SignalManager.ToastLength.SHORT)
                    }
                } else {
                    FireStoreManager.getInstance().addFriend(id) {
                        SignalManager.getInstance()
                            .toast("Friend added", SignalManager.ToastLength.SHORT)
                    }
                }

            }

        }

        setupTabsLogic()


    }


    private fun refreshCurrentTab() {
        when (binding.friendInfoTabLayout.selectedTabPosition) {
            0 -> showFriendsTab()
            1 -> showWatchlistTab()
        }
    }


    private fun setupTabsLogic() {
        binding.friendInfoTabLayout.addOnTabSelectedListener(object :
            com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                refreshCurrentTab()
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }


    private fun showFriendsTab() {
        binding.RVFriends.visibility = View.VISIBLE
        binding.RVWatchlists.visibility = View.GONE

        currentFriendUser?.let { user ->
            FireStoreManager.getInstance().getFriendsList(user.friendsList) { friends ->
                if (friends.isEmpty()) {
                    binding.NoFriendsLBL.visibility = View.VISIBLE
                    binding.RVFriends.visibility = View.GONE
                } else {
                    binding.NoFriendsLBL.visibility = View.GONE
                    binding.RVFriends.visibility = View.VISIBLE
                    friendsAdapter.updateData(friends)
                }
            }
        }

    }


    private fun showWatchlistTab() {
        if (binding.RVFriends.isVisible)
            binding.RVFriends.visibility = View.GONE
        if (binding.NoFriendsLBL.isVisible)
            binding.NoFriendsLBL.visibility = View.GONE

        binding.RVWatchlists.visibility = View.VISIBLE

        val id = friendId ?: return



        FireStoreManager.getInstance().getFriendWatchlists(id) { watchlists ->
            Log.d(Constants.TAGS.LISTS_TAG, "Updating Watchlists")
            watchlistAdapter.updateData(watchlists)

        }


    }

    override fun onStart() {
        super.onStart()
        friendId?.let { id ->
            friendshipListener = FireStoreManager.getInstance().observeFriendship(id) { status ->
                isFriend = status
                updateButtonUI(status)

                FireStoreManager.getInstance().getFriendProfile(id) { user ->
                    this.currentFriendUser = user
                    user?.let {
                        binding.friendName.text = it.username
                        binding.friendEmail.text = it.email
                        if (it.profileImageUrl.isNotEmpty()) {
                            ImageLoader.getInstance()
                                .loadProfile(it.profileImageUrl, binding.friendProfileIMG)
                        }

                        refreshCurrentTab()
                    }

                }

            }

        }
    }


    override fun onStop() {
        super.onStop()
        friendshipListener?.remove()
        friendshipListener = null
    }

//change look of friend button based on friendship status
    private fun updateButtonUI(friendStatus: Boolean) {
        if (friendStatus) {
            binding.BTNFriendAction.setText(R.string.remove_friend)
            binding.BTNFriendAction.isActivated = true
        } else {
            binding.BTNFriendAction.setText(R.string.add_friend)
            binding.BTNFriendAction.isActivated = false
        }


    }
}