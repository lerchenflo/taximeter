package com.lerchenflo.taximeter.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lerchenflo.taximeter.datasource.database.AppDatabase

fun androidAppDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val dbFile = context.getDatabasePath(AppDatabase.DB_NAME)
    return Room.databaseBuilder<AppDatabase>(
        context = context.applicationContext,
        name = dbFile.absolutePath,
    )
}
