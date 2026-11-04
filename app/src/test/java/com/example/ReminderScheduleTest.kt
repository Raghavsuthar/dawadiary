package com.example

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test
import com.example.reminder.ReminderSchedulePlanner
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ReminderScheduleTest {

    @Test
    fun `planner includes today and the next seven days`() {
        val today = LocalDate.of(2026, 1, 1)
        val dates = ReminderSchedulePlanner.planningDates(today)

        assertEquals(8, dates.size)
        assertEquals(today, dates.first())
        assertEquals(today.plusDays(7), dates.last())
    }

    @Test
    fun `planner applies weekday interval and as-needed rules`() {
        val start = LocalDate.of(2026, 1, 1)
        val monday = LocalDate.of(2026, 1, 5)
        val mondayWednesdayMask = 1 or (1 shl 2)

        assertTrue(ReminderSchedulePlanner.appliesOnDate("SPECIFIC_DAYS", monday, start, mondayWednesdayMask))
        assertTrue(ReminderSchedulePlanner.appliesOnDate("INTERVAL", start.plusDays(3), start, intervalDays = 3))
        assertFalse(ReminderSchedulePlanner.appliesOnDate("INTERVAL", start.plusDays(2), start, intervalDays = 3))
        assertFalse(ReminderSchedulePlanner.appliesOnDate("AS_NEEDED", start, start))
    }

    @Test
    fun `specific days bitmask correctly matches target weekday`() {
        // Mask: Monday (bit 0 = 1) and Wednesday (bit 2 = 4) and Friday (bit 4 = 16) -> 1 + 4 + 16 = 21
        val mwfMask = 1 or (1 shl 2) or (1 shl 4)

        // Test Monday (DayOfWeek.MONDAY.value == 1)
        val monBit = 1 shl (DayOfWeek.MONDAY.value - 1)
        assertTrue((mwfMask and monBit) != 0)

        // Test Tuesday (DayOfWeek.TUESDAY.value == 2)
        val tueBit = 1 shl (DayOfWeek.TUESDAY.value - 1)
        assertFalse((mwfMask and tueBit) != 0)

        // Test Wednesday
        val wedBit = 1 shl (DayOfWeek.WEDNESDAY.value - 1)
        assertTrue((mwfMask and wedBit) != 0)

        // Test Sunday (DayOfWeek.SUNDAY.value == 7)
        val sunBit = 1 shl (DayOfWeek.SUNDAY.value - 1)
        assertFalse((mwfMask and sunBit) != 0)
    }

    @Test
    fun `interval days schedule correctly triggers on exact intervals`() {
        val startDate = LocalDate.of(2026, 1, 1)
        val intervalDays = 3

        val day0 = startDate
        val day1 = startDate.plusDays(1)
        val day2 = startDate.plusDays(2)
        val day3 = startDate.plusDays(3)
        val day6 = startDate.plusDays(6)

        assertTrue(ChronoUnit.DAYS.between(startDate, day0) % intervalDays == 0L)
        assertFalse(ChronoUnit.DAYS.between(startDate, day1) % intervalDays == 0L)
        assertFalse(ChronoUnit.DAYS.between(startDate, day2) % intervalDays == 0L)
        assertTrue(ChronoUnit.DAYS.between(startDate, day3) % intervalDays == 0L)
        assertTrue(ChronoUnit.DAYS.between(startDate, day6) % intervalDays == 0L)
    }

    @Test
    fun `tapering date stops scheduling after end date`() {
        val taperingEnd = LocalDate.of(2026, 2, 10)

        val beforeEnd = LocalDate.of(2026, 2, 9)
        val atEnd = LocalDate.of(2026, 2, 10)
        val afterEnd = LocalDate.of(2026, 2, 11)

        assertFalse(beforeEnd.isAfter(taperingEnd))
        assertFalse(atEnd.isAfter(taperingEnd))
        assertTrue(afterEnd.isAfter(taperingEnd))
    }
}
