package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.model.MilkEntry
import com.example.data.model.MilkSession
import com.example.ui.MonthSummary
import java.util.Calendar

data class TenDayPeriodSummary(
  val periodNumber: Int, // 1, 2, or 3
  val labelMarathi: String,
  val labelEnglish: String,
  val dateRangeText: String,
  val totalLiters: Double,
  val morningLiters: Double,
  val eveningLiters: Double,
  val avgFat: Double,
  val avgSnf: Double,
  val avgRate: Double,
  val totalAmount: Double,
  val entriesCount: Int,
  val entries: List<MilkEntry>
)

object WhatsAppShareHelper {

  /**
   * Computes 10-day period summaries (1st-10th, 11th-20th, 21st-End of Month) for the given month.
   */
  fun calculateTenDayPeriods(
    entries: List<MilkEntry>,
    year: Int,
    month: Int
  ): List<TenDayPeriodSummary> {
    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.MONTH, month)
    val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    fun getPeriodForDay(dayOfMonth: Int): Int {
      return when {
        dayOfMonth in 1..10 -> 1
        dayOfMonth in 11..20 -> 2
        else -> 3
      }
    }

    val period1List = mutableListOf<MilkEntry>()
    val period2List = mutableListOf<MilkEntry>()
    val period3List = mutableListOf<MilkEntry>()

    entries.forEach { entry ->
      cal.timeInMillis = entry.date
      val entryYear = cal.get(Calendar.YEAR)
      val entryMonth = cal.get(Calendar.MONTH)
      val entryDay = cal.get(Calendar.DAY_OF_MONTH)

      if (entryYear == year && entryMonth == month) {
        when (getPeriodForDay(entryDay)) {
          1 -> period1List.add(entry)
          2 -> period2List.add(entry)
          3 -> period3List.add(entry)
        }
      }
    }

    fun buildPeriodSummary(pNum: Int, list: List<MilkEntry>, startDay: Int, endDay: Int): TenDayPeriodSummary {
      val totalLit = list.sumOf { it.liters }
      val mornLit = list.filter { it.session == MilkSession.MORNING }.sumOf { it.liters }
      val eveLit = list.filter { it.session == MilkSession.EVENING }.sumOf { it.liters }
      val totalAmt = list.sumOf { it.totalAmount }

      val fatList = list.map { it.fat }.filter { it > 0 }
      val avgFat = if (fatList.isNotEmpty()) fatList.average() else 0.0

      val snfList = list.map { it.snf }.filter { it > 0 }
      val avgSnf = if (snfList.isNotEmpty()) snfList.average() else 0.0

      val avgRate = if (totalLit > 0) totalAmt / totalLit else 0.0

      val monthName = DateUtils.formatMonthYear(DateUtils.getStartOfMonth(year, month))

      return TenDayPeriodSummary(
        periodNumber = pNum,
        labelMarathi = "१०-दिवसीय बिल कालावधी $pNum ($startDay ते $endDay)",
        labelEnglish = "10-Day Period $pNum ($startDay to $endDay)",
        dateRangeText = "$startDay ते $endDay $monthName",
        totalLiters = totalLit,
        morningLiters = mornLit,
        eveningLiters = eveLit,
        avgFat = avgFat,
        avgSnf = avgSnf,
        avgRate = avgRate,
        totalAmount = totalAmt,
        entriesCount = list.size,
        entries = list.sortedBy { it.date }
      )
    }

    return listOf(
      buildPeriodSummary(1, period1List, 1, 10),
      buildPeriodSummary(2, period2List, 11, 20),
      buildPeriodSummary(3, period3List, 21, maxDays)
    )
  }

  /**
   * Generates a beautifully formatted Marathi WhatsApp message for 10-day period bill report.
   */
  fun generateTenDayBillingWhatsAppText(
    farmName: String,
    periodSummary: TenDayPeriodSummary,
    ownerName: String = "SK Dairy Farm"
  ): String {
    val builder = StringBuilder()
    builder.appendLine("🥛 *${farmName.ifBlank { "एस. के. डेअरी (SK Dairy)" }}*")
    builder.appendLine("📋 *१०-दिवसीय दूध बिल अहवाल (10-Day Milk Bill)*")
    builder.appendLine("━━━━━━━━━━━━━━━━━━━")
    builder.appendLine("📅 *बिल कालावधी:* ${periodSummary.dateRangeText}")
    builder.appendLine("🔢 *एकूण नोंदी:* ${periodSummary.entriesCount} नोंदी")
    builder.appendLine("━━━━━━━━━━━━━━━━━━━")
    builder.appendLine("🥛 *एकूण दूध (Total):* ${String.format("%.1f", periodSummary.totalLiters)} लिटर")
    builder.appendLine("   ☀️ सकाळ संकलन: ${String.format("%.1f", periodSummary.morningLiters)} लिटर")
    builder.appendLine("   🌙 संध्याकाळ संकलन: ${String.format("%.1f", periodSummary.eveningLiters)} लिटर")

    if (periodSummary.avgFat > 0) {
      builder.appendLine("🧈 *सरासरी फॅट (Avg Fat):* ${String.format("%.1f", periodSummary.avgFat)}%")
    }
    if (periodSummary.avgSnf > 0) {
      builder.appendLine("🧪 *सरासरी एस.एन.एफ (Avg SNF):* ${String.format("%.1f", periodSummary.avgSnf)}")
    }
    if (periodSummary.avgRate > 0) {
      builder.appendLine("🏷️ *सरासरी दर (Avg Rate):* ₹${String.format("%.2f", periodSummary.avgRate)} / लिटर")
    }

    builder.appendLine("━━━━━━━━━━━━━━━━━━━")
    builder.appendLine("💰 *एकूण देय रक्कम (Total Bill):* ${DateUtils.formatCurrency(periodSummary.totalAmount)}")
    builder.appendLine("━━━━━━━━━━━━━━━━━━━")
    builder.appendLine("📲 *SK Dairy App द्वारे व्युत्पन्न*")
    return builder.toString()
  }

  /**
   * Generates a Marathi WhatsApp message for the whole Monthly Summary report.
   */
  fun generateMonthlySummaryWhatsAppText(
    farmName: String,
    monthSummary: MonthSummary
  ): String {
    val builder = StringBuilder()
    builder.appendLine("🥛 *${farmName.ifBlank { "एस. के. डेअरी (SK Dairy)" }}*")
    builder.appendLine("📊 *मासिक दूध व नफा अहवाल (${monthSummary.monthName})*")
    builder.appendLine("━━━━━━━━━━━━━━━━━━━")
    builder.appendLine("📅 *महिना:* ${monthSummary.monthName}")
    builder.appendLine("🔢 *एकूण नोंदी:* ${monthSummary.entriesCount} नोंदी")
    builder.appendLine("━━━━━━━━━━━━━━━━━━━")
    builder.appendLine("🥛 *एकूण दूध (Milk):* ${String.format("%.1f", monthSummary.totalLiters)} लिटर")
    builder.appendLine("   ☀️ सकाळ: ${String.format("%.1f", monthSummary.morningLiters)} L  |  🌙 संध्याकाळ: ${String.format("%.1f", monthSummary.eveningLiters)} L")

    if (monthSummary.avgFat > 0) {
      builder.appendLine("🧈 *सरासरी फॅट:* ${String.format("%.1f", monthSummary.avgFat)}%")
    }
    if (monthSummary.avgRate > 0) {
      builder.appendLine("🏷️ *सरासरी दर:* ₹${String.format("%.2f", monthSummary.avgRate)}/L")
    }

    builder.appendLine("💵 *दूध उत्पन्न:* ${DateUtils.formatCurrency(monthSummary.totalRevenue)}")
    builder.appendLine("🧾 *गोठा खर्च:* ${DateUtils.formatCurrency(monthSummary.totalExpenses)}")
    builder.appendLine("━━━━━━━━━━━━━━━━━━━")
    val profitSign = if (monthSummary.netProfit >= 0) "✅ *निव्वळ नफा (Net Profit):*" else "⚠️ *निव्वळ तोटा (Net Loss):*"
    builder.appendLine("$profitSign ${DateUtils.formatCurrency(monthSummary.netProfit)}")
    builder.appendLine("━━━━━━━━━━━━━━━━━━━")
    builder.appendLine("📲 *SK Dairy App द्वारे व्युत्पन्न*")
    return builder.toString()
  }

  /**
   * Dispatches direct WhatsApp share intent if installed, or fallback to generic chooser.
   */
  fun shareViaWhatsApp(context: Context, messageText: String) {
    try {
      val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        setPackage("com.whatsapp")
        putExtra(Intent.EXTRA_TEXT, messageText)
      }
      context.startActivity(intent)
    } catch (e: Exception) {
      try {
        // Try WhatsApp Business package
        val bzIntent = Intent(Intent.ACTION_SEND).apply {
          type = "text/plain"
          setPackage("com.whatsapp.w4b")
          putExtra(Intent.EXTRA_TEXT, messageText)
        }
        context.startActivity(bzIntent)
      } catch (ex: Exception) {
        // Fallback to standard share chooser
        val chooserIntent = Intent.createChooser(
          Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, messageText)
          },
          "Share Report via WhatsApp"
        )
        context.startActivity(chooserIntent)
      }
    }
  }
}
