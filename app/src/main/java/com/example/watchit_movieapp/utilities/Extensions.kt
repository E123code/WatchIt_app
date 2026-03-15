package com.example.watchit_movieapp.utilities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.watchit_movieapp.DetailsActivity
import com.example.watchit_movieapp.model.MediaItem

//extension functions to open the detailed page about a media item

/**
 * for fragments gets media item and shows the details activity
 */
fun Fragment.openDetails(item: MediaItem) {
   requireContext().openDetails(item)
}

/**
 * for activities, gets media item and shows the details activity
 * gets media Item extracts its ID and media type and sends to details activity
 */
 fun Context.openDetails(item: MediaItem) {
    val intent = Intent(this, DetailsActivity::class.java)
    var bundle = Bundle()
    bundle.putInt(Constants.bundlekeys.ID_KEY, item.id.toInt())
    bundle.putString(Constants.bundlekeys.TYPE_KEY, item.mediaType)
    intent.putExtras(bundle)
    startActivity(intent)
}