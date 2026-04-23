package com.lerchenflo.taximeter.datasource.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.lerchenflo.taximeter.datasource.database.entities.Passenger
import com.lerchenflo.taximeter.datasource.database.entities.Route
import com.lerchenflo.taximeter.datasource.database.entities.RoutePoint

@Database(
    entities = [Passenger::class, Route::class, RoutePoint::class],
    version = 3,
    exportSchema = true
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun passengerDao(): PassengerDao
    abstract fun routeDao(): RouteDao
    abstract fun routePointDao(): RoutePointDao

    companion object {
        const val DB_NAME = "database.db"
    }
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>
