package com.lerchenflo.androidapp

import android.app.Application


class MainApp: Application() {

    override fun onCreate() {
        super.onCreate()

        //startKoinAndroid(this@MainApp)

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->

            //logCrash(throwable)

            defaultHandler?.uncaughtException(thread, throwable)
        }

    }
}