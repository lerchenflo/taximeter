package com.lerchenflo.taximeter.utilities

sealed interface DataError {
    enum class Database : DataError {
        INSERT_FAILED,
        NOT_FOUND,
        UNKNOWN
    }

    enum class Location : DataError {
        PERMISSION_DENIED,
        GPS_UNAVAILABLE
    }
}
