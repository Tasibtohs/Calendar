package com.example.util

import java.util.Calendar

object CalendarConverters {

    // Bangla Month Names in Bengali and English
    private val BANGLA_MONTHS_BN = listOf(
        "বৈশাখ", "জ্যৈষ্ঠ", "আষাঢ়", "শ্রাবণ", "ভাদ্র", "আশ্বিন",
        "কার্তিক", "অগ্রহায়ণ", "পৌষ", "মাঘ", "ফাল্গুন", "চৈত্র"
    )

    private val BANGLA_MONTHS_EN = listOf(
        "Boishakh", "Jaistha", "Ashar", "Shrabon", "Bhadra", "Ashwin",
        "Kartik", "Agrahayan", "Poush", "Magh", "Falgun", "Chaitra"
    )

    // Hijri Month Names in Bengali and English
    private val HIJRI_MONTHS_BN = listOf(
        "মহররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি", "জুমাদাল আউয়াল", "জুমাদাল উখরা",
        "রজব", "শা'বান", "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ্জ"
    )

    private val HIJRI_MONTHS_EN = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani", "Jumada al-Awwal", "Jumada al-Thani",
        "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )

    data class BanglaDate(
        val day: Int,
        val monthNameBn: String,
        val monthNameEn: String,
        val year: Int,
        val formattedBn: String,
        val formattedEn: String
    )

    data class HijriDate(
        val day: Int,
        val monthNameBn: String,
        val monthNameEn: String,
        val year: Int,
        val formattedBn: String,
        val formattedEn: String
    )

    /**
     * Converts a Gregorian Calendar date to Bangla Calendar date based on Bangladesh official standard.
     * New Year starts on April 14 (Pohela Boishakh).
     */
    fun getBanglaDate(calendar: Calendar): BanglaDate {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) // 0-indexed
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val isLeap = isGregorianLeapYear(year)

        var banglaDay: Int
        var banglaMonthIndex: Int
        var banglaYear: Int

        // Determine Bangla Year
        banglaYear = if (month > 3 || (month == 3 && day >= 14)) {
            year - 593
        } else {
            year - 594
        }

        // Days in Bengali months (April 14 start)
        // Boishakh (31), Jaistha (31), Ashar (31), Shrabon (31), Bhadra (31)
        // Ashwin (30), Kartik (30), Agrahayan (30), Poush (30), Magh (30)
        // Falgun (30/31 in leap year), Chaitra (30)

        when (month) {
            Calendar.JANUARY -> { // Jan 1 - 14: Poush, Jan 15 - 31: Magh
                if (day <= 14) {
                    banglaMonthIndex = 8 // Poush
                    banglaDay = day + 17
                } else {
                    banglaMonthIndex = 9 // Magh
                    banglaDay = day - 14
                }
            }
            Calendar.FEBRUARY -> { // Feb 1 - 13: Magh, Feb 14 - 28/29: Falgun
                if (day <= 13) {
                    banglaMonthIndex = 9 // Magh
                    banglaDay = day + 17
                } else {
                    banglaMonthIndex = 10 // Falgun
                    banglaDay = day - 13
                }
            }
            Calendar.MARCH -> { // Mar 1 - 14: Falgun, Mar 15 - 31: Chaitra
                val falgunOffset = if (isLeap) 15 else 14
                if (day <= falgunOffset) {
                    banglaMonthIndex = 10 // Falgun
                    banglaDay = day + (if (isLeap) 16 else 15)
                } else {
                    banglaMonthIndex = 11 // Chaitra
                    banglaDay = day - falgunOffset
                }
            }
            Calendar.APRIL -> { // Apr 1 - 13: Chaitra, Apr 14 - 30: Boishakh
                if (day <= 13) {
                    banglaMonthIndex = 11 // Chaitra
                    banglaDay = day + 17
                } else {
                    banglaMonthIndex = 0 // Boishakh
                    banglaDay = day - 13
                }
            }
            Calendar.MAY -> { // May 1 - 14: Boishakh, May 15 - 31: Jaistha
                if (day <= 14) {
                    banglaMonthIndex = 0 // Boishakh
                    banglaDay = day + 17
                } else {
                    banglaMonthIndex = 1 // Jaistha
                    banglaDay = day - 14
                }
            }
            Calendar.JUNE -> { // Jun 1 - 14: Jaistha, Jun 15 - 30: Ashar
                if (day <= 14) {
                    banglaMonthIndex = 1 // Jaistha
                    banglaDay = day + 17
                } else {
                    banglaMonthIndex = 2 // Ashar
                    banglaDay = day - 14
                }
            }
            Calendar.JULY -> { // Jul 1 - 15: Ashar, Jul 16 - 31: Shrabon
                if (day <= 15) {
                    banglaMonthIndex = 2 // Ashar
                    banglaDay = day + 16
                } else {
                    banglaMonthIndex = 3 // Shrabon
                    banglaDay = day - 15
                }
            }
            Calendar.AUGUST -> { // Aug 1 - 16: Shrabon, Aug 17 - 31: Bhadra
                if (day <= 16) {
                    banglaMonthIndex = 3 // Shrabon
                    banglaDay = day + 15
                } else {
                    banglaMonthIndex = 4 // Bhadra
                    banglaDay = day - 16
                }
            }
            Calendar.SEPTEMBER -> { // Sep 1 - 16: Bhadra, Sep 17 - 30: Ashwin
                if (day <= 16) {
                    banglaMonthIndex = 4 // Bhadra
                    banglaDay = day + 15
                } else {
                    banglaMonthIndex = 5 // Ashwin
                    banglaDay = day - 16
                }
            }
            Calendar.OCTOBER -> { // Oct 1 - 16: Ashwin, Oct 17 - 31: Kartik
                if (day <= 16) {
                    banglaMonthIndex = 5 // Ashwin
                    banglaDay = day + 14
                } else {
                    banglaMonthIndex = 6 // Kartik
                    banglaDay = day - 16
                }
            }
            Calendar.NOVEMBER -> { // Nov 1 - 15: Kartik, Nov 16 - 30: Agrahayan
                if (day <= 15) {
                    banglaMonthIndex = 6 // Kartik
                    banglaDay = day + 15
                } else {
                    banglaMonthIndex = 7 // Agrahayan
                    banglaDay = day - 15
                }
            }
            Calendar.DECEMBER -> { // Dec 1 - 15: Agrahayan, Dec 16 - 31: Poush
                if (day <= 15) {
                    banglaMonthIndex = 7 // Agrahayan
                    banglaDay = day + 15
                } else {
                    banglaMonthIndex = 8 // Poush
                    banglaDay = day - 15
                }
            }
            else -> {
                banglaMonthIndex = 0
                banglaDay = 1
            }
        }

        val monthBn = BANGLA_MONTHS_BN[banglaMonthIndex]
        val monthEn = BANGLA_MONTHS_EN[banglaMonthIndex]

        val dayBn = toBanglaDigits(banglaDay)
        val yearBn = toBanglaDigits(banglaYear)

        return BanglaDate(
            day = banglaDay,
            monthNameBn = monthBn,
            monthNameEn = monthEn,
            year = banglaYear,
            formattedBn = "$dayBn $monthBn, $yearBn বঙ্গাব্দ",
            formattedEn = "$banglaDay $monthEn, $banglaYear BS"
        )
    }

    /**
     * Converts Gregorian Calendar date to Hijri Date using Tabular Islamic Calendar algorithm.
     * Allows dayAdjustment parameter (-2 to +2 days for moon sighting variations).
     */
    fun getHijriDate(calendar: Calendar, dayAdjustment: Int = 0): HijriDate {
        val cal = calendar.clone() as Calendar
        cal.add(Calendar.DAY_OF_MONTH, dayAdjustment)

        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)

        // Julian Day calculation
        val a = (14 - month) / 12
        val y = year + 4800 - a
        val m = month + 12 * a - 3

        var julianDay = day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045

        // Calculate Hijri year, month, day
        val l = julianDay - 1948440 + 10632
        val n = (l - 1) / 10631
        val l1 = l - 10631 * n + 354
        val j = ((10985 - l1) / 5316) * ((50 * l1) / 17719) + (l1 / 5670) * ((43 * l1) / 15238)
        val l2 = l1 - ((30 - j) / 15) * ((17719 * j) / 50) - (j / 16) * ((15238 * j) / 43) + 29
        val hijriMonth = ((24 * l2) / 709).toInt()
        val hijriDay = (l2 - ((709 * hijriMonth) / 24)).toInt()
        val hijriYear = (30 * n + j - 30).toInt()

        val safeMonthIndex = (hijriMonth - 1).coerceIn(0, 11)
        val monthBn = HIJRI_MONTHS_BN[safeMonthIndex]
        val monthEn = HIJRI_MONTHS_EN[safeMonthIndex]

        val dayBn = toBanglaDigits(hijriDay)
        val yearBn = toBanglaDigits(hijriYear)

        return HijriDate(
            day = hijriDay,
            monthNameBn = monthBn,
            monthNameEn = monthEn,
            year = hijriYear,
            formattedBn = "$dayBn $monthBn, $yearBn হিজরী",
            formattedEn = "$hijriDay $monthEn $hijriYear AH"
        )
    }

    fun toBanglaDigits(number: Int): String {
        val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        val str = number.toString()
        val sb = StringBuilder()
        for (ch in str) {
            if (ch in '0'..'9') {
                sb.append(banglaDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun getBanglaMonthSpan(calendar: Calendar): String {
        val calStart = (calendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val calEnd = (calendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, maxDay) }
        val bStart = getBanglaDate(calStart)
        val bEnd = getBanglaDate(calEnd)
        return if (bStart.monthNameBn == bEnd.monthNameBn) {
            "${bStart.monthNameBn} ${toBanglaDigits(bStart.year)}"
        } else {
            "${bStart.monthNameBn} – ${bEnd.monthNameBn} ${toBanglaDigits(bEnd.year)}"
        }
    }

    fun getHijriMonthSpan(calendar: Calendar, adjustment: Int = 0): String {
        val calStart = (calendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val calEnd = (calendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, maxDay) }
        val hStart = getHijriDate(calStart, adjustment)
        val hEnd = getHijriDate(calEnd, adjustment)
        return if (hStart.monthNameBn == hEnd.monthNameBn) {
            "${hStart.monthNameBn} ${toBanglaDigits(hStart.year)} হিঃ"
        } else {
            "${hStart.monthNameBn} – ${hEnd.monthNameBn} ${toBanglaDigits(hEnd.year)} হিঃ"
        }
    }

    private fun isGregorianLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }
}
