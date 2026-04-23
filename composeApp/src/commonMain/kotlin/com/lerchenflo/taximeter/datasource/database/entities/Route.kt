package com.lerchenflo.taximeter.datasource.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "routes",
    foreignKeys = [
        ForeignKey(
            entity = Passenger::class,
            parentColumns = ["id"],
            childColumns = ["passengerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("passengerId")]
)
data class Route(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val passengerId: Long,
    val name: String = "",
    val startTime: Long,
    val endTime: Long? = null,
    val totalDistanceMeters: Double = 0.0,
    val totalPrice: Double = 0.0,
    val isActive: Boolean = true
)
