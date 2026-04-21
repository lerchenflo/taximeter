package com.lerchenflo.taximeter.datasource.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.lerchenflo.taximeter.datasource.database.entities.Passenger
import com.lerchenflo.taximeter.datasource.database.entities.Route
import com.lerchenflo.taximeter.datasource.database.entities.RoutePoint
import kotlinx.coroutines.flow.Flow

@Dao
interface PassengerDao {
    @Insert
    suspend fun insert(passenger: Passenger): Long

    @Query("SELECT * FROM passengers ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Passenger>>

    @Query("SELECT * FROM passengers WHERE id = :id")
    suspend fun getById(id: Long): Passenger?

    @Query("DELETE FROM passengers WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface RouteDao {
    @Insert
    suspend fun insert(route: Route): Long

    @Query("SELECT * FROM routes WHERE passengerId = :passengerId ORDER BY startTime DESC")
    fun getByPassengerId(passengerId: Long): Flow<List<Route>>

    @Query("SELECT * FROM routes WHERE id = :id")
    suspend fun getById(id: Long): Route?

    @Update
    suspend fun update(route: Route)

    @Query("SELECT * FROM routes WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveRoute(): Route?
}

@Dao
interface RoutePointDao {
    @Insert
    suspend fun insert(routePoint: RoutePoint): Long

    @Query("SELECT * FROM route_points WHERE routeId = :routeId ORDER BY timestamp ASC")
    fun getByRouteId(routeId: Long): Flow<List<RoutePoint>>
}
