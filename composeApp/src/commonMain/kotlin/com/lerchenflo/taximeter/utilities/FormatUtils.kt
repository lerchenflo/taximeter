package com.lerchenflo.taximeter.utilities

import kotlin.math.roundToInt

fun Double.formatPrice(): String {
    val rounded = (this * 100).roundToInt()
    val whole = rounded / 100
    val frac = rounded % 100
    return "$whole.${frac.toString().padStart(2, '0')}"
}

fun Double.formatDistance(): String {
    val km = this / 1000.0
    return "${km.formatPrice()} km"
}

fun Long.formatDuration(): String {
    val h = this / 3600
    val m = (this % 3600) / 60
    val s = this % 60
    return if (h > 0) {
        "$h:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
    } else {
        "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
    }
}

fun Long.formatDateTime(): String {
    val totalSeconds = this / 1000
    val totalMinutes = totalSeconds / 60
    val totalHours = totalMinutes / 60
    val totalDays = totalHours / 24

    val second = (totalSeconds % 60).toInt()
    val minute = (totalMinutes % 60).toInt()
    val hour = (totalHours % 24).toInt()

    var days = totalDays.toInt()
    var year = 1970
    while (true) {
        val daysInYear = if (isLeapYear(year)) 366 else 365
        if (days < daysInYear) break
        days -= daysInYear
        year++
    }

    val monthDays = if (isLeapYear(year)) {
        intArrayOf(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    } else {
        intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    }

    var month = 0
    while (month < 12 && days >= monthDays[month]) {
        days -= monthDays[month]
        month++
    }
    val day = days + 1
    month += 1

    return "${day.toString().padStart(2, '0')}.${month.toString().padStart(2, '0')}.$year " +
            "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
}

private fun isLeapYear(year: Int): Boolean =
    (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
