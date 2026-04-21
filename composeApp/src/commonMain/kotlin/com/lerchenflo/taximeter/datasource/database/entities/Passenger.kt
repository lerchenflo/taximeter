package com.lerchenflo.taximeter.datasource.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("passengers")
data class Passenger(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val createdAt: Long
)
