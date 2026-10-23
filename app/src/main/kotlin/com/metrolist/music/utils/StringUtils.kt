/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.utils

import java.util.Locale

fun makeTimeString(duration: Long?): String {
    if (duration == null || duration < 0) return ""
    var sec = duration / 1000
    val day = sec / 86400
    sec %= 86400
    val hour = sec / 3600
    sec %= 3600
    val minute = sec / 60
    sec %= 60
    return when {
        day > 0 -> "%d:%02d:%02d:%02d".format(day, hour, minute, sec)
        hour > 0 -> "%d:%02d:%02d".format(hour, minute, sec)
        else -> "%d:%02d".format(minute, sec)
    }
}

/**
 * Formats a duration in milliseconds to precise timestamp format `mm:ss.t` or `hh:mm:ss.t`.
 *
 * @param duration Duration in milliseconds.
 * @return Formatted precise timestamp string.
 */
fun formatPreciseTime(duration: Long?): String {
    if (duration == null || duration < 0) return "--:--.-"
    val totalTenths = duration / 100
    val tenths = totalTenths % 10
    val totalSeconds = duration / 1000
    val seconds = totalSeconds % 60
    val minutes = totalSeconds / 60
    val hours = minutes / 60
    val remMinutes = minutes % 60

    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d.%d", hours, remMinutes, seconds, tenths)
    } else {
        String.format(Locale.US, "%02d:%02d.%d", remMinutes, seconds, tenths)
    }
}

fun joinByBullet(vararg str: String?) =
    str
        .filterNot {
            it.isNullOrEmpty()
        }.joinToString(separator = " • ")
