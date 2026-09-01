package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayShortFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
    private val displayFullFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val displayPrettyFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
    private val monthIsoFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val displayMonthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val displayMonthShortFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())

    fun todayIso(): String {
        return isoFormat.format(Date())
    }

    fun yesterdayIso(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return isoFormat.format(cal.time)
    }

    fun thisMonthIso(): String {
        return monthIsoFormat.format(Date())
    }

    fun thisYearIso(): String {
        return SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
    }

    fun extractMonthIso(isoDate: String): String {
        return if (isoDate.length >= 7) isoDate.substring(0, 7) else thisMonthIso()
    }

    fun extractYearIso(isoDate: String): String {
        return if (isoDate.length >= 4) isoDate.substring(0, 4) else thisYearIso()
    }

    fun formatToMonthDisplay(monthIso: String): String {
        return try {
            val date = monthIsoFormat.parse(monthIso) ?: return monthIso
            displayMonthFormat.format(date)
        } catch (_: Exception) {
            monthIso
        }
    }

    fun formatToMonthShortDisplay(monthIso: String): String {
        return try {
            val date = monthIsoFormat.parse(monthIso) ?: return monthIso
            displayMonthShortFormat.format(date)
        } catch (_: Exception) {
            monthIso
        }
    }

    fun formatToShortDisplay(isoDate: String): String {
        return try {
            val date = isoFormat.parse(isoDate) ?: return isoDate
            displayShortFormat.format(date)
        } catch (e: Exception) {
            isoDate
        }
    }

    fun formatToFullDisplay(isoDate: String): String {
        return try {
            val date = isoFormat.parse(isoDate) ?: return isoDate
            displayFullFormat.format(date)
        } catch (e: Exception) {
            isoDate
        }
    }

    fun formatToPrettyDisplay(isoDate: String): String {
        return try {
            val date = isoFormat.parse(isoDate) ?: return isoDate
            displayPrettyFormat.format(date)
        } catch (e: Exception) {
            isoDate
        }
    }

    fun parseAnyInputToIso(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null

        // Try YYYY-MM-DD
        try {
            if (trimmed.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
                isoFormat.parse(trimmed)
                return trimmed
            }
        } catch (_: Exception) {}

        // Try DD/MM/YYYY
        try {
            if (trimmed.matches(Regex("^\\d{1,2}/\\d{1,2}/\\d{4}$"))) {
                val date = displayFullFormat.parse(trimmed)
                if (date != null) return isoFormat.format(date)
            }
        } catch (_: Exception) {}

        // Try DD/MM (assume current year)
        try {
            if (trimmed.matches(Regex("^\\d{1,2}/\\d{1,2}$"))) {
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val full = "$trimmed/$currentYear"
                val date = displayFullFormat.parse(full)
                if (date != null) return isoFormat.format(date)
            }
        } catch (_: Exception) {}

        // Try DD-MM-YYYY
        try {
            val altFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            if (trimmed.matches(Regex("^\\d{1,2}-\\d{1,2}-\\d{4}$"))) {
                val date = altFormat.parse(trimmed)
                if (date != null) return isoFormat.format(date)
            }
        } catch (_: Exception) {}

        return null
    }

    fun formatFromYMD(year: Int, month1Based: Int, day: Int): String {
        return String.format(Locale.US, "%04d-%02d-%02d", year, month1Based, day)
    }

    fun parseIsoToCalendar(isoDate: String): Calendar? {
        return try {
            val date = isoFormat.parse(isoDate) ?: return null
            val cal = Calendar.getInstance()
            cal.time = date
            cal
        } catch (_: Exception) {
            null
        }
    }

    fun getRecentDaysIso(count: Int): List<String> {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        for (i in 0 until count) {
            list.add(isoFormat.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return list.reversed()
    }
}
