package com.ivy.wallet.base


import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit


fun timeNowLocal() = LocalDateTime.now()

fun timeNowUTC(): LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)

fun dateNowUTC(): LocalDate = LocalDate.now(ZoneOffset.UTC)

fun startOfDayNowUTC() = dateNowUTC().atStartOfDay()

fun endOfDayNowUTC() = dateNowUTC().atEndOfDay()

fun Long.epochSecondToDateTime(): LocalDateTime =
    LocalDateTime.ofEpochSecond(this, 0, ZoneOffset.UTC)

fun LocalDateTime.toEpochSeconds() = this.toEpochSecond(ZoneOffset.UTC)

fun Long.epochMilliToDateTime(): LocalDateTime =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDateTime()

fun LocalDateTime.toEpochMilli(): Long = millis()

fun LocalDateTime.millis() = this.toInstant(ZoneOffset.UTC).toEpochMilli()

fun LocalDateTime.formatNicely(
    pattern: String = "EEE, dd MMM",
    patternNoWeekDay: String = "dd MMM",
    zone: ZoneId = ZoneOffset.systemDefault()
): String {
    val today = dateNowUTC()
    return when (this.toLocalDate()) {
        today -> {
            "Today, ${this.formatLocal(patternNoWeekDay, zone)}"
        }
        today.minusDays(1) -> {
            "Yesterday, ${this.formatLocal(patternNoWeekDay, zone)}"
        }
        today.plusDays(1) -> {
            "Tomorrow, ${this.formatLocal(patternNoWeekDay, zone)}"
        }
        else -> {
            this.formatLocal(pattern, zone)
        }
    }
}

fun LocalDateTime.formatNicelyWithTime(
    noWeekDay: Boolean = false,
    pattern: String = "EEE, dd MMM 'at' HH:mm",
    patternNoWeekDay: String = "dd MMM 'at' HH:mm",
    zone: ZoneId = ZoneOffset.systemDefault()
): String {
    val today = dateNowUTC()

    if (noWeekDay) {
        return this.formatLocal(patternNoWeekDay)
    }

    return when (this.toLocalDate()) {
        today -> {
            "Today, ${this.formatLocal(patternNoWeekDay, zone)}"
        }
        today.minusDays(1) -> {
            "Yesterday, ${this.formatLocal(patternNoWeekDay, zone)}"
        }
        today.plusDays(1) -> {
            "Tomorrow, ${this.formatLocal(patternNoWeekDay, zone)}"
        }
        else -> {
            this.formatLocal(pattern, zone)
        }
    }
}

@Composable
fun LocalDateTime.formatLocalTime(): String {
    val timeFormat = android.text.format.DateFormat.getTimeFormat(LocalContext.current)
    return timeFormat.format(this.millis())
}

fun LocalDate.formatDateOnly(): String = this.formatLocal("dd MMM", ZoneOffset.systemDefault())

fun LocalDate.formatDateOnlyWithYear(): String =
    this.formatLocal("dd MMM, yyyy", ZoneOffset.systemDefault())


fun LocalDate.formatDateWeekDay(): String =
    this.formatLocal("EEE, dd MMM", ZoneOffset.systemDefault())

fun LocalDate.formatDateWeekDayLong(): String =
    this.formatLocal("EEEE, dd MMM", ZoneOffset.systemDefault())

fun LocalDate.formatNicely(
    pattern: String = "EEE, dd MMM",
    patternNoWeekDay: String = "dd MMM",
    zone: ZoneId = ZoneOffset.systemDefault()
): String {
    val closeDay = closeDay()
    return if (closeDay != null)
        "$closeDay, ${this.formatLocal(patternNoWeekDay, zone)}" else this.formatLocal(
        pattern,
        zone
    )
}

fun LocalDate.closeDay(): String? {
    val today = dateNowUTC()
    return when (this) {
        today -> {
            "Today"
        }
        today.minusDays(1) -> {
            "Yesterday"
        }
        today.plusDays(1) -> {
            "Tomorrow"
        }
        else -> {
            null
        }
    }
}

fun LocalDateTime.formatLocal(
    pattern: String = "dd MMM yyyy, HH:mm",
    zone: ZoneId = ZoneOffset.systemDefault()
): String {
    val localDateTime = this.convertUTCtoLocal(zone)
    return localDateTime.atZone(zone).format(
        DateTimeFormatter
            .ofPattern(pattern)
            .withLocale(Locale.getDefault())
            .withZone(zone) //this is if you want to display the Zone in the pattern
    )
}

fun LocalDateTime.convertUTCtoLocal(zone: ZoneId = ZoneOffset.systemDefault()): LocalDateTime {
    return this.convertUTCto(zone)
}

fun LocalDateTime.convertUTCto(zone: ZoneId): LocalDateTime {
    return plusSeconds(atZone(zone).offset.totalSeconds.toLong())
}

fun LocalTime.convertLocalToUTC(): LocalTime {
    val offset = timeNowLocal().atZone(ZoneOffset.systemDefault()).offset.totalSeconds.toLong()
    return this.minusSeconds(offset)
}

fun LocalDateTime.convertLocalToUTC(): LocalDateTime {
    val offset = timeNowLocal().atZone(ZoneOffset.systemDefault()).offset.totalSeconds.toLong()
    return this.minusSeconds(offset)
}


fun LocalDate.formatLocal(
    pattern: String = "dd MMM yyyy",
    zone: ZoneId = ZoneOffset.systemDefault()
): String {
    return this.format(
        DateTimeFormatter
            .ofPattern(pattern)
            .withLocale(Locale.getDefault())
            .withZone(zone) //this is if you want to display the Zone in the pattern
    )
}

fun LocalDateTime.timeLeft(
    from: LocalDateTime = timeNowUTC(),
    daysLabel: String = "d",
    hoursLabel: String = "h",
    minutesLabel: String = "m",
    secondsLabel: String = "s"
): String {
    val timeLeftMs = this.millis() - from.millis()
    if (timeLeftMs <= 0) return "Expired"

    val days = TimeUnit.MILLISECONDS.toDays(timeLeftMs)
    var timeLeftAfterCalculations = timeLeftMs - TimeUnit.DAYS.toMillis(days)

    val hours = TimeUnit.MILLISECONDS.toHours(timeLeftAfterCalculations)
    timeLeftAfterCalculations -= TimeUnit.HOURS.toMillis(hours)

    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeftAfterCalculations)
    timeLeftAfterCalculations -= TimeUnit.MINUTES.toMillis(minutes)

    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeLeftAfterCalculations)

    var result = ""
    if (days > 0) {
        result += "$days$daysLabel "
    }
    if (hours > 0) {
        result += "$hours$hoursLabel "
    }
    if (minutes > 0) {
        result += "$minutes$minutesLabel "
    }
//    if (seconds > 0) {
//        result += "$seconds$secondsLabel "
//    }

    return result.trim()
}

fun startOfMonth(date: LocalDate): LocalDateTime =
    date.withDayOfMonth(1).atStartOfDay()

fun endOfMonth(date: LocalDate): LocalDateTime =
    date.withDayOfMonth(date.lengthOfMonth()).atEndOfDay()

fun LocalDate.atEndOfDay(): LocalDateTime =
    this.atTime(23, 59, 59)

/**
 * +1 day so things won't fck up with Long overflow
 */
fun beginningOfIvyTime(): LocalDateTime = LocalDateTime.now().minusYears(10)

fun LocalDate.withDayOfMonthSafe(targetDayOfMonth: Int): LocalDate {
    val maxDayOfMonth = this.lengthOfMonth()
    return this.withDayOfMonth(
        if (targetDayOfMonth > maxDayOfMonth) maxDayOfMonth else targetDayOfMonth
    )
}