package com.example.watchit_movieapp.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.watchit_movieapp.AuthLoginActivity
import com.example.watchit_movieapp.FriendProfileActivity
import com.example.watchit_movieapp.SearchUsersActivity
import com.example.watchit_movieapp.adapters.UserAdapter
import com.example.watchit_movieapp.databinding.ProfileFragmentBinding
import com.example.watchit_movieapp.interfaces.UserClickedCallback
import com.example.watchit_movieapp.utilities.Constants
import com.example.watchit_movieapp.utilities.FireStoreManager
import com.example.watchit_movieapp.utilities.ImageLoader
import com.example.watchit_movieapp.utilities.SignalManager
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private lateinit var userAdapter: UserAdapter


    private var _binding: ProfileFragmentBinding? = null
    private val binding get() = _binding!!

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")
                uploadImage(uri)
            } else {
                Log.d("PhotoPicker", "No media selected")
                SignalManager.getInstance()
                    .toast("No image selected", SignalManager.ToastLength.SHORT)
            }

        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ProfileFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()

    }


    private fun initViews() {

        setupRecycleView()
        binding.UploadBTN.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.searchFriendsBTN.setOnClickListener {
            openUsersSearch()

        }

        binding.logoutBTN.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(requireContext(), AuthLoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            activity?.finish()
        }

        loadUser()
    }


    private fun loadUser() {
        val user = FireStoreManager.currentUser ?: return


        binding.Name.text = user.username
        binding.email.text = user.email
        if (user.profileImageUrl.isNotEmpty()) {
            ImageLoader.getInstance().loadProfile(user.profileImageUrl, binding.ProfileIMG)
        }


        FireStoreManager.getFriendsList(user.friendsList) { users ->
            if (!isAdded) return@getFriendsList
            userAdapter.updateData(users)

            if (users.isNotEmpty()) {
                binding.rvFriends.visibility = View.VISIBLE
                binding.NoFriendsLBL.visibility = View.GONE
            } else {
                binding.rvFriends.visibility = View.GONE
                binding.NoFriendsLBL.visibility = View.VISIBLE
            }
        }


    }

    private fun setupRecycleView() {
        val callback = object : UserClickedCallback {
            override fun userClicked(userId: String) {
                val intent = Intent(requireContext(), FriendProfileActivity::class.java)
                var bundle = Bundle()
                bundle.putString(Constants.bundlekeys.ID_KEY, userId)

                intent.putExtras(bundle)
                startActivity(intent)
            }
        }

        userAdapter = UserAdapter(emptyList(), callback)

        binding.rvFriends.adapter = userAdapter
        binding.rvFriends.layoutManager = LinearLayoutManager(requireContext())

    }


    private fun openUsersSearch() {
        val intent = Intent(requireContext(), SearchUsersActivity::class.java)
        startActivity(intent)
    }

    private fun uploadImage(uri: Uri) {
        FireStoreManager.uploadProfileImage(uri) { profileUri ->
            if (profileUri != null) {
                SignalManager.getInstance()
                    .toast("Profile updated!", SignalManager.ToastLength.SHORT)
            } else {
                SignalManager.getInstance()
                    .toast("Failed to upload", SignalManager.ToastLength.SHORT)
            }

        }


    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            loadUser()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isHidden) {
            loadUser()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}