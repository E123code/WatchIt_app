package com.example.watchit_movieapp

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment


import com.example.watchit_movieapp.databinding.ActivityMainBinding
import com.example.watchit_movieapp.fragments.HomeFragment
import com.example.watchit_movieapp.fragments.ListsFragment
import com.example.watchit_movieapp.fragments.ProfileFragment
import com.example.watchit_movieapp.fragments.SearchFragment
import com.example.watchit_movieapp.utilities.Constants
import com.example.watchit_movieapp.utilities.FireStoreManager
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding


    private lateinit var homeFragment: HomeFragment
    private lateinit var searchFragment: SearchFragment
    private lateinit var listsFragment: ListsFragment
    private lateinit var profileFragment: ProfileFragment

    private lateinit var activeFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            FireStoreManager.checkUserSaved(it)
        }

        if (savedInstanceState == null) {

            homeFragment = HomeFragment()
            searchFragment = SearchFragment()
            listsFragment = ListsFragment()
            profileFragment = ProfileFragment()

            activeFragment = homeFragment

            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, profileFragment, Constants.TAGS.PROFILE_TAG).hide(profileFragment)
                .add(R.id.fragment_container, listsFragment, Constants.TAGS.LISTS_TAG).hide(listsFragment)
                .add(R.id.fragment_container, searchFragment, Constants.TAGS.SEARCH_TAG).hide(searchFragment)
                .add(R.id.fragment_container, homeFragment, Constants.TAGS.HOME_TAG)
                .commit()

            binding.bottomNavigation.selectedItemId = R.id.home
        }else{

            homeFragment = supportFragmentManager.findFragmentByTag(Constants.TAGS.HOME_TAG) as HomeFragment
            searchFragment = supportFragmentManager.findFragmentByTag(Constants.TAGS.SEARCH_TAG) as SearchFragment
            listsFragment = supportFragmentManager.findFragmentByTag(Constants.TAGS.LISTS_TAG) as ListsFragment
            profileFragment = supportFragmentManager.findFragmentByTag(Constants.TAGS.PROFILE_TAG) as ProfileFragment

            activeFragment = supportFragmentManager.fragments.find { !it.isHidden } ?: homeFragment
        }

        initViews()


    }

    private  fun initViews(){


        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    switchFragment(homeFragment)
                    Log.i("HOME","home selected")
                }
                R.id.search -> {
                    switchFragment(searchFragment)
                    Log.i("SEARCH","search selected")
                }
                R.id.watchlists -> {
                    switchFragment(listsFragment)
                    Log.i("LISTS","lists selected")
                }
                R.id.profile -> {
                    switchFragment(profileFragment)
                    Log.i("PROFILE","profile selected")
                }
            }
            true
        }


        supportFragmentManager.addOnBackStackChangedListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            when (currentFragment) {
                is HomeFragment -> {
                    binding.bottomNavigation.menu.findItem(R.id.home).isChecked = true
                }
                is SearchFragment -> {
                    binding.bottomNavigation.menu.findItem(R.id.search).isChecked = true
                }
                is ListsFragment -> {
                    binding.bottomNavigation.menu.findItem(R.id.watchlists).isChecked = true
                }
                is ProfileFragment -> {
                    binding.bottomNavigation.menu.findItem(R.id.profile).isChecked = true
                }
            }
        }



    }

    fun navigateToSearch() {
        binding.bottomNavigation.selectedItemId = R.id.search
    }

    private fun switchFragment(targetFragment: Fragment) {
        if (targetFragment == activeFragment) return

        val transaction = supportFragmentManager.beginTransaction()

        if (!targetFragment.isAdded) {
            transaction.add(R.id.fragment_container, targetFragment)
        }


        transaction.hide(activeFragment).show(targetFragment)


        if (targetFragment !is HomeFragment) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
        activeFragment = targetFragment
    }



}