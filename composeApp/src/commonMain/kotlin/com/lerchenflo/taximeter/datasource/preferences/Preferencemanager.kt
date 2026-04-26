package com.lerchenflo.taximeter.datasource.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lerchenflo.taximeter.settings.domain.SpeedScale
import com.lerchenflo.taximeter.settings.domain.VehicleType
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
        val IDLE_RATE = stringPreferencesKey("idle_rate")
        val VEHICLE_TYPE = stringPreferencesKey("vehicle_type")
        val SPEED_SCALE = stringPreferencesKey("speed_scale")
        val GPS_INTERVAL_MS = longPreferencesKey("gps_interval_ms")
        val GPS_MIN_DISTANCE_M = floatPreferencesKey("gps_min_distance_m")
    }

    companion object {
        const val DEFAULT_BASE_FARE = 3.50
        const val DEFAULT_PRICE_PER_KM = 1.80
        const val DEFAULT_IDLE_RATE = 0.35
        const val DEFAULT_GPS_INTERVAL_MS = 2000L
        const val DEFAULT_GPS_MIN_DISTANCE_M = 5f
    }

    suspend fun clearAll() {
        prefs.edit { it.clear() }
    }

    suspend fun saveUseSounds(value: Boolean) {
        prefs.edit { it[PrefsKeys.USE_SOUNDS] = value }
    }

    fun getUseSoundsFlow(): Flow<Boolean> = prefs.data.map { prefs ->
        prefs[PrefsKeys.USE_SOUNDS] ?: false
    }

    suspend fun getUseSounds(): Boolean {
        return prefs.data.first()[PrefsKeys.USE_SOUNDS] ?: false
    }

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

    suspend fun saveIdleRate(value: Double) {
        prefs.edit { it[PrefsKeys.IDLE_RATE] = value.toString() }
    }

    suspend fun getIdleRate(): Double {
        return prefs.data.first()[PrefsKeys.IDLE_RATE]?.toDoubleOrNull() ?: DEFAULT_IDLE_RATE
    }

    suspend fun saveGpsIntervalMs(value: Long) {
        prefs.edit { it[PrefsKeys.GPS_INTERVAL_MS] = value }
    }

    fun getGpsIntervalMsFlow(): Flow<Long> = prefs.data.map { prefs ->
        prefs[PrefsKeys.GPS_INTERVAL_MS] ?: DEFAULT_GPS_INTERVAL_MS
    }

    suspend fun getGpsIntervalMs(): Long {
        return prefs.data.first()[PrefsKeys.GPS_INTERVAL_MS] ?: DEFAULT_GPS_INTERVAL_MS
    }

    suspend fun saveGpsMinDistanceM(value: Float) {
        prefs.edit { it[PrefsKeys.GPS_MIN_DISTANCE_M] = value }
    }

    fun getGpsMinDistanceMFlow(): Flow<Float> = prefs.data.map { prefs ->
        prefs[PrefsKeys.GPS_MIN_DISTANCE_M] ?: DEFAULT_GPS_MIN_DISTANCE_M
    }

    suspend fun getGpsMinDistanceM(): Float {
        return prefs.data.first()[PrefsKeys.GPS_MIN_DISTANCE_M] ?: DEFAULT_GPS_MIN_DISTANCE_M
    }

    suspend fun saveSpeedScale(scale: SpeedScale) {
        prefs.edit { it[PrefsKeys.SPEED_SCALE] = scale.name }
    }

    fun getSpeedScaleFlow(): Flow<SpeedScale> = prefs.data.map { p ->
        p[PrefsKeys.SPEED_SCALE]?.let { runCatching { SpeedScale.valueOf(it) }.getOrNull() } ?: SpeedScale.MEDIUM_FAST
    }

    suspend fun getSpeedScale(): SpeedScale {
        val raw = prefs.data.first()[PrefsKeys.SPEED_SCALE]
        return raw?.let { runCatching { SpeedScale.valueOf(it) }.getOrNull() } ?: SpeedScale.MEDIUM_FAST
    }

    suspend fun saveVehicleType(type: VehicleType) {
        prefs.edit { it[PrefsKeys.VEHICLE_TYPE] = type.name }
    }

    fun getVehicleTypeFlow(): Flow<VehicleType> = prefs.data.map { prefs ->
        prefs[PrefsKeys.VEHICLE_TYPE]?.let { runCatching { VehicleType.valueOf(it) }.getOrNull() } ?: VehicleType.CAR
    }

    suspend fun getVehicleType(): VehicleType {
        val raw = prefs.data.first()[PrefsKeys.VEHICLE_TYPE]
        return raw?.let { runCatching { VehicleType.valueOf(it) }.getOrNull() } ?: VehicleType.CAR
    }
}
