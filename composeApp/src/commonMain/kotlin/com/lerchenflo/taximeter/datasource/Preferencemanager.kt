package com.lerchenflo.taximeter.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class Preferencemanager(
    private val prefs: DataStore<Preferences>,
) {

    private object PrefsKeys {
        val USE_SOUNDS = booleanPreferencesKey("use_sounds")
        val BASE_FARE = stringPreferencesKey("base_fare")
        val PRICE_PER_KM = stringPreferencesKey("price_per_km")
    }

    companion object {
        const val DEFAULT_BASE_FARE = 3.50
        const val DEFAULT_PRICE_PER_KM = 1.80
    }

    suspend fun clearAll() {
        prefs.edit { it.clear() }
    }

    // Sounds
    suspend fun saveUseSounds(value: Boolean) {
        prefs.edit { it[PrefsKeys.USE_SOUNDS] = value }
    }

    fun getUseSoundsFlow(): Flow<Boolean> = prefs.data.map { prefs ->
        prefs[PrefsKeys.USE_SOUNDS] ?: false
    }

    suspend fun getUseSounds(): Boolean {
        return prefs.data.first()[PrefsKeys.USE_SOUNDS] ?: false
    }

    // Pricing
    suspend fun saveBaseFare(value: Double) {
        prefs.edit { it[PrefsKeys.BASE_FARE] = value.toString() }
    }

    fun getBaseFareFlow(): Flow<Double> = prefs.data.map { prefs ->
        prefs[PrefsKeys.BASE_FARE]?.toDoubleOrNull() ?: DEFAULT_BASE_FARE
    }

    suspend fun getBaseFare(): Double {
        return prefs.data.first()[PrefsKeys.BASE_FARE]?.toDoubleOrNull() ?: DEFAULT_BASE_FARE
    }

    suspend fun savePricePerKm(value: Double) {
        prefs.edit { it[PrefsKeys.PRICE_PER_KM] = value.toString() }
    }

    fun getPricePerKmFlow(): Flow<Double> = prefs.data.map { prefs ->
        prefs[PrefsKeys.PRICE_PER_KM]?.toDoubleOrNull() ?: DEFAULT_PRICE_PER_KM
    }

    suspend fun getPricePerKm(): Double {
        return prefs.data.first()[PrefsKeys.PRICE_PER_KM]?.toDoubleOrNull() ?: DEFAULT_PRICE_PER_KM
    }
}
