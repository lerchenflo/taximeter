package com.lerchenflo.taximeter.datasource.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserSetting(
    @PrimaryKey val id: Long = 0L,

    val name: String,
    val multiplier: Long = 1L

)