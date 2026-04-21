package com.lerchenflo.taximeter.core.presentation

import com.lerchenflo.taximeter.core.domain.util.DataError

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
}

fun DataError.toUiText(): UiText {
    return when (this) {
        DataError.Database.INSERT_FAILED -> UiText.DynamicString("Failed to save data.")
        DataError.Database.NOT_FOUND -> UiText.DynamicString("Data not found.")
        DataError.Database.UNKNOWN -> UiText.DynamicString("An unknown error occurred.")
        DataError.Location.PERMISSION_DENIED -> UiText.DynamicString("Location permission denied.")
        DataError.Location.GPS_UNAVAILABLE -> UiText.DynamicString("GPS is not available.")
    }
}
