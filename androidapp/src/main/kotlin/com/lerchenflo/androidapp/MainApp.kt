package com.lerchenflo.androidapp

import android.app.Application
import com.lerchenflo.taximeter.di.startKoinAndroid

class MainApp: Application() {

    override fun onCreate() {
        super.onCreate()

        onAppStart()

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->

            //logCrash(throwable)

            defaultHandler?.uncaughtException(thread, throwable)
        }

    }

    fun onAppStart() {
        startKoinAndroid(this@MainApp)
    }
}