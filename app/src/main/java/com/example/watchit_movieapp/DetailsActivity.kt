package com.example.watchit_movieapp

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.watchit_movieapp.adapters.CastAdapter
import com.example.watchit_movieapp.adapters.ProviderAdapter
import com.example.watchit_movieapp.databinding.ActivityDetailsBinding
import com.example.watchit_movieapp.model.ProviderItem
import com.example.watchit_movieapp.model.TitleDetails
import com.example.watchit_movieapp.utilities.Constants

import com.example.watchit_movieapp.utilities.ImageLoader
import com.example.watchit_movieapp.utilities.RetrofitClient
import com.example.watchit_movieapp.utilities.SignalManager
import kotlinx.coroutines.launch


class DetailsActivity : AppCompatActivity() {

    private lateinit var binding : ActivityDetailsBinding

    // אדפטרים לשחקנים וספקים
    private lateinit var castAdapter: CastAdapter
    private lateinit var providerAdapter: ProviderAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
    }


    private fun initViews(){
        val bundle: Bundle? = intent.extras
        val id = bundle?.getInt(Constants.bundlekeys.ID_KEY, -1) ?: -1
        val type = bundle?.getString(Constants.bundlekeys.TYPE_KEY) ?: "movie"

        Log.d("TEST_DETAILS", "Received ID: $id")
        Log.d("TEST_DETAILS", "Received Type: $type")

        setupRecyclerViews()

        binding.BTNBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }



        if (id != -1) {
            fetchDetails(id, type)
        }
    }

    private fun setupRecyclerViews() {
        //Actors List
        castAdapter = CastAdapter()
        binding.rvCast.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvCast.adapter = castAdapter

        // Providers List
        providerAdapter = ProviderAdapter()
        binding.rvProviders.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvProviders.adapter = providerAdapter
    }

    private fun fetchDetails(Id: Int, type:String){
        lifecycleScope.launch {
            try {

                val response = RetrofitClient.getDetails(Id,type)

                response.mediaType =type

                updateUI(response)
            } catch (e: Exception) {
                Log.e("Details", "Error: ${e.message}")
                SignalManager.getInstance().toast("Failed to load data", SignalManager.ToastLength.SHORT)
            }
        }

    }

    private fun updateUI(details: TitleDetails) {
        binding.Title.text = details.name
        binding.ageRating.text= details.ageRating
        binding.releaseYearDurationLine.text = "${details.date} • ${details.duration}"
        binding.genresDetails.text = details.genres
        binding.ratingBar.rating = (details.rating/2).toFloat()
        binding.userRating.visibility= View.GONE

        val israelProviders = details.watchProviders?.results?.get("IL")?.membership
        updateProviders(israelProviders)

        binding.Overview.text = details.overview

        ImageLoader.getInstance().loadImage(details.fullPosterUrl, binding.IMGPoster)
        castAdapter.updateData(details.credits?.cast)


    }

    private fun updateProviders(providers: List<ProviderItem>?) {
        if (providers.isNullOrEmpty()) {
            // אם הרשימה ריקה או null
            binding.rvProviders.visibility = View.GONE
            binding.NoProviders.visibility = View.VISIBLE
        } else {
            // אם יש נתונים
            binding.rvProviders.visibility = View.VISIBLE
            binding.NoProviders.visibility = View.GONE
            providerAdapter.updateData(providers)
        }
    }
}