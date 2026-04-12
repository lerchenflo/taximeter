package com.lerchenflo.taximeter.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceKey
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.Foundation.URLForDirectory
import kotlin.experimental.ExperimentalForeignApi

fun iosDatastoreBuilder(): DataStore<Preferences> {
    val documentDir = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    val dbPath = documentDir?.path + "/settings.preferences_pb"
    return createDataStore {
        filePath = dbPath
    }
}
