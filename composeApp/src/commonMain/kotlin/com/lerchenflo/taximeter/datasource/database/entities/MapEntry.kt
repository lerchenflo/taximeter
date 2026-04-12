package com.lerchenflo.taximeter.datasource.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Instant

@Entity("mapEntrys")
data class MapEntry(
    @PrimaryKey val id: Long = 0L,

    val routeId: Long, //Each route has his own id

    val latitude: Double,
    val longitude: Double,
    val timeStamp: Long
)