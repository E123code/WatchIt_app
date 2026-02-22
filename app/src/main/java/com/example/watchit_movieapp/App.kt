package com.example.watchit_movieapp

import android.app.Application
import com.example.watchit_movieapp.utilities.ImageLoader
import com.example.watchit_movieapp.utilities.SignalManager


class App: Application() {
    override fun onCreate() {
        super.onCreate()
        SignalManager.init(this)
        ImageLoader.init(this)

    }
}