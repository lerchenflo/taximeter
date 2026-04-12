package com.lerchenflo.taximeter.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class Preferencemanager(
    private val prefs: DataStore<Preferences>,
) {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private object PrefsKeys {
        val USE_SOUNDS = booleanPreferencesKey("use_sounds")
    }


    suspend fun clearAll() {
        prefs.edit { it.clear() }
    }




    // Markdown Format
    suspend fun saveUseSounds(value: Boolean) {
        prefs.edit { it[PrefsKeys.USE_SOUNDS] = value }
    }

    fun getUseSoundsFlow(): Flow<Boolean> = prefs.data.map { prefs ->
        prefs[PrefsKeys.USE_SOUNDS] ?: false //Default to false
    }

    suspend fun getUseSounds(): Boolean {
        return prefs.data.first()[PrefsKeys.USE_SOUNDS] ?: false
    }

}