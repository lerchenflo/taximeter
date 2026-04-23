package com.lerchenflo.taximeter.datasource.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "route_points",
    foreignKeys = [
        ForeignKey(
            entity = Route::class,
            parentColumns = ["id"],
            childColumns = ["routeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("routeId")]
)
data class RoutePoint(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val routeId: Long,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val speed: Float = 0f
)
