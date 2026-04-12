package com.lerchenflo.taximeter.datasource.database

import androidx.room.Dao
import androidx.room.Upsert
import com.lerchenflo.taximeter.datasource.database.entities.MapEntry

@Dao
interface MapEntryDao {
    @Upsert()
    suspend fun upsert(mapEntry: MapEntry)
}