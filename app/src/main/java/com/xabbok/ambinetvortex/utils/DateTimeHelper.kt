package com.xabbok.ambinetvortex.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun currentDate(): Date = Calendar.getInstance(TimeZone.getDefault()).time

fun hoursBetween(start: Date, end: Date): Long {
    val calendar = Calendar.getInstance(TimeZone.getDefault())
    calendar.time = start
    val startMillis = calendar.timeInMillis

    calendar.time = end
    val endMillis = calendar.timeInMillis

    val diffMillis = endMillis - startMillis
    val diffHours = diffMillis / (60 * 60 * 1000)

    return diffHours
}

fun roundDateByDay(date: Date) : Date {
    val calendar = Calendar.getInstance()
    calendar.time = date
    calendar[Calendar.HOUR_OF_DAY] = 1
    calendar[Calendar.MINUTE] = 0
    calendar[Calendar.SECOND] = 0
    calendar[Calendar.MILLISECOND] = 0
    return calendar.time
}

fun formatDateWithoutYear(date: Date) : String {
    return SimpleDateFormat("d MMMM", Locale.getDefault()).format(date)
}

fun formatDateWithYear(date: Date) : String {
    return SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(date)
}

fun getDateYear(date: Date) : Int {
    val calendar = Calendar.getInstance()
    calendar.time = date
    return calendar.get(Calendar.YEAR)
}

fun atEndOfDay(date: Date): Date {
    val calendar = Calendar.getInstance()
    calendar.time = date
    calendar[Calendar.HOUR_OF_DAY] = 23
    calendar[Calendar.MINUTE] = 59
    calendar[Calendar.SECOND] = 59
    calendar[Calendar.MILLISECOND] = 999
    return calendar.time
}

fun atStartOfDay(date: Date): Date {
    val calendar = Calendar.getInstance()
    calendar.time = date
    calendar[Calendar.HOUR_OF_DAY] = 0
    calendar[Calendar.MINUTE] = 0
    calendar[Calendar.SECOND] = 0
    calendar[Calendar.MILLISECOND] = 0
    return calendar.time
}

fun epochToDate(epochSecond: Long): Date {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = epochSecond * 1000
    return calendar.time
}