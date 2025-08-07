package com.example.eventuretest

import com.google.firebase.FirebaseApp
import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ClaudiaApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}