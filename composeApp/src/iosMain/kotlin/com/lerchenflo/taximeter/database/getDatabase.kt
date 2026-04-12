package com.lerchenflo.taximeter.database

import androidx.room.Room
import com.lerchenflo.taximeter.datasource.database.AppDatabase
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.Foundation.URLForDirectory
import kotlin.experimental.ExperimentalForeignApi

fun iosAppDatabaseBuilder(): Room.databaseBuilder<AppDatabase> {
    val documentDir = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    val dbFile = documentDir?.path + "/" + AppDatabase.DB_NAME
    return Room.databaseBuilder<AppDatabase>(name = dbFile)
}
