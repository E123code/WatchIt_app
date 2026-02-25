package com.example.watchit_movieapp.utilities

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.watchit_movieapp.DetailsActivity
import com.example.watchit_movieapp.model.MediaItem


 fun Fragment.openDetails(item: MediaItem) {
    val intent = Intent(requireContext(), DetailsActivity::class.java)
    var bundle = Bundle()
    bundle.putInt(Constants.bundlekeys.ID_KEY, item.id)
    bundle.putString(Constants.bundlekeys.TYPE_KEY, item.mediaType) // "movie" או "tv" שהזרקנו ב-loadMovies/TV
    intent.putExtras(bundle)
    startActivity(intent)
}