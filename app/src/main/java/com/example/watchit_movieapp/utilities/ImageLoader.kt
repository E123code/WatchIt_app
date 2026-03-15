package com.example.watchit_movieapp.utilities

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.watchit_movieapp.R
import java.lang.ref.WeakReference

//class of glide to handle image showing
class ImageLoader private constructor(context: Context) {
    private val contextRef = WeakReference(context)

    companion object {
        @Volatile
        private var instance: ImageLoader? = null
        fun init(context: Context): ImageLoader {
            return instance ?: synchronized(this) {
                instance
                    ?: ImageLoader(context).also { instance = it }
            }
        }

        fun getInstance(): ImageLoader {
            return instance ?: throw IllegalStateException(
                "ImageLoader must be initialized by calling init(context) before use."
            )

        }
    }

//loads image
    fun loadImage(
        source: String,
        imageView: ImageView,
        placeHolder: Int = R.drawable.unavailable_photo
    ) {
        contextRef.get()?.let { context ->
            Glide
                .with(context)
                .load(source)
                .centerCrop()
                .placeholder(placeHolder)
                .into(imageView)
        }
    }

    //load profile image (for user or cast member)
    fun loadProfile(
        source: String,
        imageView: ImageView,
        placeHolder: Int = R.drawable.round_profile_circle,
        progressBar: View? = null//in case we upload profile picture, we will see it while it loads the picture
    ) {
        contextRef.get()?.let { context ->
            progressBar?.visibility = View.VISIBLE


            Glide
                .with(context)
                .load(source)
                .circleCrop()
                .placeholder(placeHolder)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar?.visibility = View.GONE
                        return false
                    }
                    override fun onResourceReady(
                        resource: Drawable?,
                        _model: Any?,
                        _target: Target<Drawable>?,
                        _dataSource: DataSource?,
                        _isFirst: Boolean
                    ): Boolean {
                        progressBar?.visibility = View.GONE
                        return false
                    }

                })
                .into(imageView)
        }
    }


}