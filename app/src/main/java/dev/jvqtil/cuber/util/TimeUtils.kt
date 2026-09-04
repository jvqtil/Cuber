package dev.jvqtil.cuber.util

import dev.jvqtil.cuber.database.PENALTY_PLUS_TWO
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TimeUtils {

    fun format(ms: Long): String {
        val minutes = ms / 60_000
        val seconds = (ms / 1_000) % 60
        val centiseconds = (ms % 1_000) / 10

        return if (minutes > 0) {
            "$minutes:%02d.%02d".format(
                seconds,
                centiseconds
            )
        } else {
            "%d.%02d".format(
                seconds,
                centiseconds
            )
        }
    }

    fun formatNullable(ms: Long?): String {
        return ms?.let(::format) ?: "—"
    }

    fun formatSolveTime(
        time: Long,
        penalty: String
    ): String {
        return format(
            when (penalty) {
                PENALTY_PLUS_TWO -> time + 2_000
                else -> time
            }
        )
    }

    fun formatTimeOfDay(timestamp: Long): String {
        return SimpleDateFormat(
            "HH:mm",
            Locale.getDefault()
        ).format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        return SimpleDateFormat(
            "d MMMM yyyy, HH:mm",
            Locale.getDefault()
        ).format(Date(timestamp))
    }

    fun formatDate(timestamp: Long): String {
        return SimpleDateFormat(
            "d MMMM yyyy",
            Locale.getDefault()
        ).format(Date(timestamp))
    }

    fun isSameDay(
        first: Calendar,
        second: Calendar
    ): Boolean {
        return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
                first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR)
    }
}