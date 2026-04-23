package com.lerchenflo.taximeter.datasource.repository

import com.lerchenflo.taximeter.utilities.currentTimeMillis
import com.lerchenflo.taximeter.datasource.database.RouteDao
import com.lerchenflo.taximeter.datasource.database.RoutePointDao
import com.lerchenflo.taximeter.datasource.database.entities.Route
import com.lerchenflo.taximeter.datasource.database.entities.RoutePoint
import com.lerchenflo.taximeter.datasource.database.entities.RouteWithPassenger
import kotlinx.coroutines.flow.Flow

class RouteRepository(
    private val routeDao: RouteDao,
    private val routePointDao: RoutePointDao
) {
    fun getRoutesForPassenger(passengerId: Long): Flow<List<Route>> =
        routeDao.getByPassengerId(passengerId)

    suspend fun getRouteById(id: Long): Route? = routeDao.getById(id)

    suspend fun getActiveRoute(): Route? = routeDao.getActiveRoute()

    suspend fun startRoute(passengerId: Long): Long {
        val route = Route(
            passengerId = passengerId,
            startTime = currentTimeMillis()
        )
        return routeDao.insert(route)
    }

    suspend fun finishRoute(route: Route) {
        routeDao.update(
            route.copy(
                endTime = currentTimeMillis(),
                isActive = false
            )
        )
    }

    suspend fun updateRoute(route: Route) {
        routeDao.update(route)
    }

    suspend fun addRoutePoint(routeId: Long, latitude: Double, longitude: Double): Long {
        val point = RoutePoint(
            routeId = routeId,
            latitude = latitude,
            longitude = longitude,
            timestamp = currentTimeMillis()
        )
        return routePointDao.insert(point)
    }

    fun getRoutePoints(routeId: Long): Flow<List<RoutePoint>> =
        routePointDao.getByRouteId(routeId)

    fun getAllRoutes(): Flow<List<Route>> = routeDao.getAll()

    fun getAllRoutesWithPassenger(): Flow<List<RouteWithPassenger>> =
        routeDao.getAllWithPassengerName()

    suspend fun getRoutePointsOnce(routeId: Long): List<RoutePoint> =
        routePointDao.getByRouteIdOnce(routeId)
}
