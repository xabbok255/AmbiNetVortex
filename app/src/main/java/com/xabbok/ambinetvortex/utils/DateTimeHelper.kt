package com.xabbok.ambinetvortex.utils

import java.util.Calendar
import java.util.Date
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

fun epochToDate(epochSecond: Long): Date {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = epochSecond * 1000
    return calendar.time
}