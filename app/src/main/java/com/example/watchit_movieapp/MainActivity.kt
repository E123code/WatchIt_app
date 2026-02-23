package com.example.watchit_movieapp

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment


import com.example.watchit_movieapp.databinding.ActivityMainBinding
import com.example.watchit_movieapp.fragments.HomeFragment
import com.example.watchit_movieapp.fragments.ListsFragment
import com.example.watchit_movieapp.fragments.ProfileFragment
import com.example.watchit_movieapp.fragments.SearchFragment



class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding


    private val homeFragment = HomeFragment()
    private val searchFragment = SearchFragment()
    private val listsFragment = ListsFragment()
    private val profileFragment = ProfileFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    replaceFragment(homeFragment)
                    Log.i("HOME","home selected")
                }
                R.id.search -> {
                    replaceFragment(searchFragment)
                    Log.i("SEARCH","search selected")
                }
                R.id.watchlists -> {
                    replaceFragment(listsFragment)
                    Log.i("LISTS","lists selected")
                }
                R.id.profile -> {
                    replaceFragment(profileFragment)
                    Log.i("PROFILE","profile selected")
                }
            }
            true
        }

        if(savedInstanceState == null) {
            replaceFragment(homeFragment) // טוען את הפרגמנט
            binding.bottomNavigation.selectedItemId = R.id.home // גורם לאייקון להפוך לצהוב
        }


    }

    fun navigateToSearch() {
        binding.bottomNavigation.selectedItemId = R.id.search
    }

    private fun replaceFragment(fragment: Fragment){
        val  transaction =supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container,fragment)

        if (fragment !is HomeFragment) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
    }



}