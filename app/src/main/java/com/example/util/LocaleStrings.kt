package com.example.util

enum class AppLanguage {
  MARATHI,
  ENGLISH
}

object AppStrings {
  // Navigation
  fun tabMilk(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "दूध नोंद" else "Milk Log"
  fun tabBreeding(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "कृत्रिम रेतन व प्रसूती" else "AI & Breeding"
  fun tabCows(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "गाईंची यादी" else "Cattle Herd"
  fun tabProfit(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "नफा-तोटा गणक" else "Profit Calculator"
  fun tabAlerts(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "सूचना व अलर्ट" else "Alerts"

  // App Title
  fun appTitle(lang: AppLanguage) = "SK Dairy"

  // Buttons & Actions
  fun addMilk(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "+ दूध नोंद करा" else "+ Record Milk"
  fun addAI(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "+ कृत्रिम रेतन (AI) नोंद" else "+ Add AI Record"
  fun addCow(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "+ नवीन गाय जोडा" else "+ Add New Cow"
  fun addExpense(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "+ खर्च नोंदवा" else "+ Add Expense"
  fun save(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "जतन करा (Save)" else "Save"
  fun cancel(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "रद्द करा" else "Cancel"
  fun confirm(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "निश्चित करा" else "Confirm"
  fun delete(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "हटवा" else "Delete"

  // Milk Log & Sessions
  fun morning(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "सकाळ" else "Morning"
  fun evening(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "संध्याकाळ" else "Evening"
  fun liters(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "लिटर" else "Liters"
  fun ratePerLiter(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "दर (₹/लिटर)" else "Rate (₹/L)"
  fun fat(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "फॅट (Fat %)" else "Fat %"
  fun snf(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "एसएनएफ (SNF)" else "SNF"
  fun totalMilk(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "एकूण दूध" else "Total Milk"
  fun totalIncome(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "एकूण उत्पन्न" else "Total Income"

  // Breeding & AI
  fun aiDate(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "कृत्रिम रेतन (AI) तारीख" else "AI Date"
  fun pregCheckDate(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "गर्भ तपासणी तारीख (~६० दिवस)" else "Pregnancy Check Date (~60 Days)"
  fun expectedDeliveryDate(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "अपेक्षित प्रसूती तारीख (~२८२ दिवस)" else "Expected Calving Date (~282 Days)"
  fun bullDetails(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "वळू / सिमेन जात व तपशील" else "Bull / Semen Details"
  fun doctorName(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "डॉक्टर / रेतन तंत्रज्ञ नाव" else "Doctor / Inseminator"
  fun markCalved(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "प्रसूती झाली (नोंद करा)" else "Record Delivery (Calved)"
  fun markPregnancyConfirmed(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "गाभण निश्चित करा" else "Confirm Pregnancy"
  fun alert5DaysTitle(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "प्रसूती ५ दिवसांत अपेक्षित" else "Calving in 5 Days"
  fun alert3MonthsTitle(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "प्रसूतीनंतर ३ महिने पूर्ण (पुढील रेतन)" else "3 Months Post-Calving (Next AI)"

  // Profit Calculator
  fun monthlySummary(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "महिना अखेर हिशोब व नफा-तोटा" else "Month-End Profit & Loss"
  fun milkRevenue(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "दूध विक्री उत्पन्न" else "Milk Sales Revenue"
  fun totalExpenses(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "एकूण खर्च (खाद्य, औषध, इ.)" else "Total Expenses"
  fun netProfit(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "निव्वळ नफा (Net Profit)" else "Net Profit"
  fun netLoss(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "तोटा (Net Loss)" else "Net Loss"
  fun feedExpenses(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "पशूखाद्य व पेंड खर्च" else "Feed & Concentrate"
  fun vetExpenses(lang: AppLanguage) = if (lang == AppLanguage.MARATHI) "वैद्यकीय व रेतन खर्च" else "Vet & AI Expenses"
}
