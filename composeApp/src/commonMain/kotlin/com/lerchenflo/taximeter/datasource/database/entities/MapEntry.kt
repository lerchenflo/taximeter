package com.lerchenflo.taximeter.datasource.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("mapEntrys")
data class MapEntry(
    @PrimaryKey val id: Long = 0L
)