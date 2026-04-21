package com.lerchenflo.taximeter.datasource.repository

import com.lerchenflo.taximeter.core.domain.util.currentTimeMillis
import com.lerchenflo.taximeter.datasource.database.PassengerDao
import com.lerchenflo.taximeter.datasource.database.entities.Passenger
import kotlinx.coroutines.flow.Flow

class PassengerRepository(
    private val passengerDao: PassengerDao
) {
    fun getAllPassengers(): Flow<List<Passenger>> = passengerDao.getAll()

    suspend fun getPassengerById(id: Long): Passenger? = passengerDao.getById(id)

    suspend fun addPassenger(name: String): Long {
        val passenger = Passenger(
            name = name,
            createdAt = currentTimeMillis()
        )
        return passengerDao.insert(passenger)
    }

    suspend fun deletePassenger(id: Long) = passengerDao.deleteById(id)
}
