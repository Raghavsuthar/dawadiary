package com.example

import com.example.util.ScheduleTimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScheduleTimeValidationTest {
    @Test
    fun `normalizes valid boundary and single-digit hour values`() {
        assertEquals("00:00,23:59,08:05", ScheduleTimeUtils.normalize("0:00,23:59,8:05"))
    }

    @Test
    fun `rejects hours and minutes outside clock ranges`() {
        assertNull(ScheduleTimeUtils.normalize("24:00"))
        assertNull(ScheduleTimeUtils.normalize("12:60"))
    }

    @Test
    fun `rejects malformed and empty entries instead of silently dropping them`() {
        assertNull(ScheduleTimeUtils.normalize("08:00,not-a-time"))
        assertNull(ScheduleTimeUtils.normalize(""))
        assertNull(ScheduleTimeUtils.normalize("08:00,"))
    }

    @Test
    fun `removes duplicate reminder times`() {
        assertEquals("08:00,20:00", ScheduleTimeUtils.normalize("8:00,08:00,20:00"))
    }
}
