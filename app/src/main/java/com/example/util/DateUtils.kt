package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
  private val displayDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
  private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
  private val shortDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
  private val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.getDefault())

  fun formatDate(timestamp: Long): String {
    return if (timestamp <= 0) "-" else displayDateFormat.format(Date(timestamp))
  }

  fun formatShortDate(timestamp: Long): String {
    return if (timestamp <= 0) "-" else shortDateFormat.format(Date(timestamp))
  }

  fun formatMonthYear(timestamp: Long): String {
    return monthYearFormat.format(Date(timestamp))
  }

  fun formatDayOfWeek(timestamp: Long): String {
    return dayOfWeekFormat.format(Date(timestamp))
  }

  fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return formatter.format(amount).replace("INR", "₹").trim()
  }

  fun formatLiters(liters: Double): String {
    return String.format(Locale.US, "%.1f L", liters)
  }

  fun getStartOfDay(timestamp: Long = System.currentTimeMillis()): Long {
    val cal = Calendar.getInstance().apply {
      timeInMillis = timestamp
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
  }

  fun getStartOfMonth(year: Int, month: Int): Long {
    val cal = Calendar.getInstance().apply {
      set(Calendar.YEAR, year)
      set(Calendar.MONTH, month)
      set(Calendar.DAY_OF_MONTH, 1)
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
  }

  fun getEndOfMonth(year: Int, month: Int): Long {
    val cal = Calendar.getInstance().apply {
      set(Calendar.YEAR, year)
      set(Calendar.MONTH, month)
      set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
      set(Calendar.HOUR_OF_DAY, 23)
      set(Calendar.MINUTE, 59)
      set(Calendar.SECOND, 59)
      set(Calendar.MILLISECOND, 999)
    }
    return cal.timeInMillis
  }
}
