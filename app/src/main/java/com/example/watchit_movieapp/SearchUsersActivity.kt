package com.example.watchit_movieapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.watchit_movieapp.adapters.UserAdapter
import com.example.watchit_movieapp.databinding.ActivitySearchUsersBinding
import com.example.watchit_movieapp.interfaces.UserClickedCallback
import com.example.watchit_movieapp.utilities.Constants
import com.example.watchit_movieapp.utilities.FireStoreManager

class SearchUsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchUsersBinding



    private  lateinit var  userAdapter: UserAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySearchUsersBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        initViews()

    }


    private fun initViews(){
        binding.BTNBack.setOnClickListener {
            finish()
        }




        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    searchUsers(query)
                    binding.searchBar.clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    binding.RVResults.visibility = View.GONE
                    binding.NoResultsLBL.visibility = View.GONE
                    userAdapter.updateData(emptyList())
                }
                return true
            }
        })

        binding.BTNSearchUser.setOnClickListener {
            val query = binding.searchBar.query.toString()
            if (query.isNotEmpty())
                searchUsers(query)
        }



    }

    private  fun setupRecyclerView(){

        val  callback = object : UserClickedCallback{
            override fun userClicked(userId: String) {
                val intent = Intent(this@SearchUsersActivity, FriendProfileActivity :: class.java)
                var bundle = Bundle()
                bundle.putString(Constants.bundlekeys.ID_KEY, userId)

                intent.putExtras(bundle)
                startActivity(intent)
            }
        }

        userAdapter = UserAdapter(emptyList(), callback)

        binding.RVResults.adapter = userAdapter
        binding.RVResults.layoutManager = LinearLayoutManager(this)


    }

    private fun searchUsers(query: String){
        val cleanQuery  = query.trim()
        FireStoreManager.getInstance().searchUsers(cleanQuery){users ->
            if(users.isEmpty()){
                binding.RVResults.visibility = View.GONE
                binding.NoResultsLBL.visibility = View.VISIBLE
            }else{
                binding.NoResultsLBL.visibility = View.GONE
                binding.RVResults.visibility = View.VISIBLE
                userAdapter.updateData(users)
            }

        }


    }
}