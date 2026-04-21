package com.lerchenflo.taximeter.core.domain.util

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
