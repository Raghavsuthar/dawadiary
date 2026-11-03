package com.example.util

import java.util.Locale

object ScheduleTimeUtils {
    /** Returns normalized comma-separated HH:mm values, or null if any entry is missing/invalid. */
    fun normalize(rawTimes: String): String? {
        val entries = rawTimes.split(',').map(String::trim)
        if (entries.isEmpty() || entries.any { !it.matches(Regex("\\d{1,2}:\\d{2}")) }) return null

        val normalized = entries.map { entry ->
            val parts = entry.split(':')
            val hour = parts[0].toIntOrNull() ?: return null
            val minute = parts[1].toIntOrNull() ?: return null
            if (hour !in 0..23 || minute !in 0..59) return null
            String.format(Locale.ROOT, "%02d:%02d", hour, minute)
        }
        return normalized.distinct().joinToString(",")
    }
}
