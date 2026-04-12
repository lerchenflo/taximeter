package com.lerchenflo.taximeter.di


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.androidDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

fun createAndroidDataStore(context: Context): DataStore<Preferences> {
    return context.androidDataStore
}
