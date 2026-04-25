package com.lerchenflo.taximeter.datasource.repository

import com.lerchenflo.taximeter.utilities.currentTimeMillis
import com.lerchenflo.taximeter.utilities.generateRandomColor
import com.lerchenflo.taximeter.datasource.database.PassengerDao
import com.lerchenflo.taximeter.datasource.database.entities.Passenger
import kotlinx.coroutines.flow.Flow

class PassengerRepository(
    private val passengerDao: PassengerDao
) {
    fun getAllPassengers(): Flow<List<Passenger>> = passengerDao.getAll()

    suspend fun getPassengerById(id: Long): Passenger? = passengerDao.getById(id)

    suspend fun addPassenger(name: String, color: Long? = null): Long {
        val passenger = Passenger(
            name = name,
            createdAt = currentTimeMillis(),
            color = color ?: generateRandomColor()
        )
        return passengerDao.insert(passenger)
    }

    suspend fun deletePassenger(id: Long) = passengerDao.deleteById(id)

    suspend fun clearAllData() = passengerDao.deleteAll()
}
