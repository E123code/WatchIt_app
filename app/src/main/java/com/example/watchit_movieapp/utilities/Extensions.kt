package com.example.watchit_movieapp.utilities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.watchit_movieapp.DetailsActivity
import com.example.watchit_movieapp.model.MediaItem


fun Fragment.openDetails(item: MediaItem) {
   requireContext().openDetails(item)
}
 fun Context.openDetails(item: MediaItem) {
    val intent = Intent(this, DetailsActivity::class.java)
    var bundle = Bundle()
    bundle.putInt(Constants.bundlekeys.ID_KEY, item.id.toInt())
    bundle.putString(Constants.bundlekeys.TYPE_KEY, item.mediaType)
    intent.putExtras(bundle)
    startActivity(intent)
}