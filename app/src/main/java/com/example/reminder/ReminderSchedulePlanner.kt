package com.example.reminder

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object ReminderSchedulePlanner {
    const val DEFAULT_LOOKAHEAD_DAYS = 7L

    /** Includes today plus each future date through the configured look-ahead. */
    fun planningDates(today: LocalDate, lookAheadDays: Long = DEFAULT_LOOKAHEAD_DAYS): List<LocalDate> =
        (0L..lookAheadDays).map(today::plusDays)

    fun appliesOnDate(
        scheduleType: String,
        date: LocalDate,
        startDate: LocalDate,
        daysOfWeekMask: Int = 0,
        intervalDays: Int = 1
    ): Boolean {
        if (date.isBefore(startDate) || scheduleType == "AS_NEEDED") return false
        return when (scheduleType) {
            "SPECIFIC_DAYS" -> {
                val dayBit = 1 shl (date.dayOfWeek.value - 1)
                (daysOfWeekMask and dayBit) != 0
            }
            "INTERVAL" -> {
                val interval = intervalDays.coerceAtLeast(1)
                ChronoUnit.DAYS.between(startDate, date) % interval == 0L
            }
            else -> true
        }
    }
}
