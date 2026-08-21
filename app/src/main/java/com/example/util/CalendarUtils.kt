package com.example.util

import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat

object CalendarUtils {

    // --- BANGLA CALENDAR ---
    data class BanglaDate(
        val day: Int,
        val monthNameEn: String,
        val monthNameBn: String,
        val year: Int,
        val formattedBn: String
    )

    private val banglaMonthNamesEn = listOf(
        "Boishakh", "Joishtho", "Asharh", "Shrabon", "Bhadro", "Ashwin",
        "Karttik", "Agrahayan", "Poush", "Magh", "Falgun", "Chaitra"
    )

    private val banglaMonthNamesBn = listOf(
        "বৈশাখ", "জ্যৈষ্ঠ", "আষাঢ়", "শ্রাবণ", "ভাদ্র", "আশ্বিন",
        "কার্তিক", "অগ্রহায়ণ", "পৌষ", "মাঘ", "ফাল্গুন", "চৈত্র"
    )

    fun toBanglaDigit(number: Int): String {
        val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        return number.toString().map { if (it in '0'..'9') bnDigits[it - '0'] else it }.joinToString("")
    }

    fun getBanglaDayName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> "রবিবার"
            Calendar.MONDAY -> "সোমবার"
            Calendar.TUESDAY -> "মঙ্গলবার"
            Calendar.WEDNESDAY -> "বুধবার"
            Calendar.THURSDAY -> "বৃহস্পতিবার"
            Calendar.FRIDAY -> "শুক্রবার"
            Calendar.SATURDAY -> "শনিবার"
            else -> ""
        }
    }

    val gregorianMonthNamesBn = listOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )

    fun getBanglaMonthYear(cal: Calendar): String {
        val monthIdx = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)
        val monthBn = gregorianMonthNamesBn.getOrElse(monthIdx) { "" }
        val yearBn = toBanglaDigit(year)
        return "$monthBn $yearBn"
    }

    data class SolarPrayerTimes(
        val fajr: String,
        val sunrise: String,
        val dhuhr: String,
        val asr: String,
        val maghrib: String,
        val isha: String,
        val shortDisplayText: String
    )

    fun getSolarPrayerTimesForDate(cal: Calendar): SolarPrayerTimes {
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val sunDeclination = 23.45 * Math.sin(2 * Math.PI * (284 + dayOfYear) / 365.0)
        val noonMinutes = 12 * 60 + 5
        val phi = Math.toRadians(23.81) // Dhaka coordinates
        val delta = Math.toRadians(sunDeclination)
        val cosHourAngle = -Math.tan(phi) * Math.tan(delta)
        val clampedCos = cosHourAngle.coerceIn(-1.0, 1.0)
        val hourAngleDeg = Math.toDegrees(Math.acos(clampedCos))
        val halfDayMinutes = (hourAngleDeg * 4).toInt()

        val sunriseMin = noonMinutes - halfDayMinutes
        val sunsetMin = noonMinutes + halfDayMinutes
        val fajrMin = sunriseMin - 78
        val dhuhrMin = noonMinutes
        val asrMin = noonMinutes + (halfDayMinutes * 0.58).toInt()
        val maghribMin = sunsetMin + 2
        val ishaMin = maghribMin + 74

        fun formatTime(totalMinutes: Int): String {
            val totalM = (totalMinutes + 24 * 60) % (24 * 60)
            val h = totalM / 60
            val m = totalM % 60
            val displayH = if (h == 0) 12 else if (h > 12) h - 12 else h
            val mStr = if (m < 10) "0$m" else "$m"
            return "${toBanglaDigit(displayH)}:${toBanglaDigit(m)}"
        }

        val sunriseStr = formatTime(sunriseMin)
        val sunsetStr = formatTime(sunsetMin)
        val fajrStr = formatTime(fajrMin)
        val dhuhrStr = formatTime(dhuhrMin)
        val asrStr = formatTime(asrMin)
        val maghribStr = formatTime(maghribMin)
        val ishaStr = formatTime(ishaMin)

        return SolarPrayerTimes(
            fajr = fajrStr,
            sunrise = sunriseStr,
            dhuhr = dhuhrStr,
            asr = asrStr,
            maghrib = maghribStr,
            isha = ishaStr,
            shortDisplayText = "সূর্যোদয় $sunriseStr"
        )
    }

    fun formatGregorianInBangla(cal: Calendar): String {
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val monthIdx = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)
        val dayBn = toBanglaDigit(day)
        val monthBn = gregorianMonthNamesBn.getOrElse(monthIdx) { "" }
        val yearBn = toBanglaDigit(year)
        return "$dayBn $monthBn $yearBn"
    }

    fun getBanglaDate(calendar: Calendar): BanglaDate {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) // 0-indexed (0 = Jan, 11 = Dec)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val isLeap = isLeapYear(year)

        var bYear = if (month > 3 || (month == 3 && day >= 14)) year - 593 else year - 594

        var bMonthIdx = 0
        var bDay = 1

        when (month) {
            Calendar.JANUARY -> {
                if (day < 15) {
                    bMonthIdx = 8 // Poush
                    bDay = day + 16
                } else {
                    bMonthIdx = 9 // Magh
                    bDay = day - 14
                }
            }
            Calendar.FEBRUARY -> {
                if (day < 14) {
                    bMonthIdx = 9 // Magh
                    bDay = day + 17
                } else {
                    bMonthIdx = 10 // Falgun
                    bDay = day - 13
                }
            }
            Calendar.MARCH -> {
                val falgunDays = if (isLeapYear(year)) 31 else 30
                if (day <= (falgunDays - 15 + 1)) {
                    bMonthIdx = 10 // Falgun
                    bDay = day + 15
                } else {
                    bMonthIdx = 11 // Chaitra
                    bDay = day - (falgunDays - 15)
                }
            }
            Calendar.APRIL -> {
                if (day < 14) {
                    bMonthIdx = 11 // Chaitra
                    bDay = day + 17
                } else {
                    bMonthIdx = 0 // Boishakh
                    bDay = day - 13
                }
            }
            Calendar.MAY -> {
                if (day < 15) {
                    bMonthIdx = 0 // Boishakh
                    bDay = day + 17
                } else {
                    bMonthIdx = 1 // Joishtho
                    bDay = day - 14
                }
            }
            Calendar.JUNE -> {
                if (day < 16) {
                    bMonthIdx = 1 // Joishtho
                    bDay = day + 16
                } else {
                    bMonthIdx = 2 // Asharh
                    bDay = day - 15
                }
            }
            Calendar.JULY -> {
                if (day < 17) {
                    bMonthIdx = 2 // Asharh
                    bDay = day + 15
                } else {
                    bMonthIdx = 3 // Shrabon
                    bDay = day - 16
                }
            }
            Calendar.AUGUST -> {
                if (day < 17) {
                    bMonthIdx = 3 // Shrabon
                    bDay = day + 15
                } else {
                    bMonthIdx = 4 // Bhadro
                    bDay = day - 16
                }
            }
            Calendar.SEPTEMBER -> {
                if (day < 17) {
                    bMonthIdx = 4 // Bhadro
                    bDay = day + 15
                } else {
                    bMonthIdx = 5 // Ashwin
                    bDay = day - 16
                }
            }
            Calendar.OCTOBER -> {
                if (day < 17) {
                    bMonthIdx = 5 // Ashwin
                    bDay = day + 14
                } else {
                    bMonthIdx = 6 // Karttik
                    bDay = day - 16
                }
            }
            Calendar.NOVEMBER -> {
                if (day < 16) {
                    bMonthIdx = 6 // Karttik
                    bDay = day + 15
                } else {
                    bMonthIdx = 7 // Agrahayan
                    bDay = day - 15
                }
            }
            Calendar.DECEMBER -> {
                if (day < 16) {
                    bMonthIdx = 7 // Agrahayan
                    bDay = day + 15
                } else {
                    bMonthIdx = 8 // Poush
                    bDay = day - 15
                }
            }
        }

        val formattedBn = "${toBanglaDigit(bDay)} ${banglaMonthNamesBn[bMonthIdx]}, ${toBanglaDigit(bYear)} বঙ্গাব্দ"
        return BanglaDate(
            day = bDay,
            monthNameEn = banglaMonthNamesEn[bMonthIdx],
            monthNameBn = banglaMonthNamesBn[bMonthIdx],
            year = bYear,
            formattedBn = formattedBn
        )
    }

    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    fun getBengaliSeason(monthNameEn: String): String {
        return when (monthNameEn) {
            "Boishakh", "Joishtho" -> "গ্রীষ্মকাল"
            "Asharh", "Shrabon" -> "বর্ষাকাল"
            "Bhadro", "Ashwin" -> "শরৎকাল"
            "Karttik", "Agrahayan" -> "হেমন্তকাল"
            "Poush", "Magh" -> "শীতকাল"
            "Falgun", "Chaitra" -> "বসন্তকাল"
            else -> "বসন্তকাল"
        }
    }

    // --- HIJRI CALENDAR ---
    data class HijriDate(
        val day: Int,
        val monthNameEn: String,
        val monthNameAr: String,
        val monthNameBn: String = "",
        val year: Int,
        val formattedEn: String,
        val formattedAr: String
    )

    private val hijriMonthNamesEn = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )

    private val hijriMonthNamesAr = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الثاني",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    private val hijriMonthNamesBn = listOf(
        "মুহাররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি",
        "জমাদিউল আউয়াল", "জমাদিউস সানি", "রজব", "শা'বান",
        "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ"
    )

    fun getHijriDate(calendar: Calendar, dayAdjustment: Int = 0): HijriDate {
        val cal = calendar.clone() as Calendar
        if (dayAdjustment != 0) {
            cal.add(Calendar.DAY_OF_YEAR, dayAdjustment)
        }

        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)

        // Julian Day Number algorithm for Tabular Hijri Calendar
        var m = month
        var y = year
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = Math.floor(y / 100.0).toInt()
        val b = 2 - a + Math.floor(a / 4.0).toInt()
        val jd = Math.floor(365.25 * (y + 4716)).toInt() + Math.floor(30.6001 * (m + 1)).toInt() + day + b - 1524

        val z = jd - 1948440 + 10632
        val n = Math.floor((z - 1) / 10631.0).toInt()
        val z1 = z - 10631 * n + 354
        val j = (Math.floor((10982 - z1) / 5316.0) * Math.floor((50 * z1) / 17719.0) + Math.floor(z1 / 5670.0) * Math.floor((43 * z1) / 15238.0)).toInt()
        val z2 = z1 - Math.floor((30 - j) / 15.0).toInt() * Math.floor((17719 * j) / 50.0).toInt() - Math.floor(j / 16.0).toInt() * Math.floor((15238 * j) / 43.0).toInt() + 29
        val hMonth = Math.floor((24 * z2) / 709.0).toInt()
        val hDay = z2 - Math.floor((709 * hMonth) / 24.0).toInt()
        val hYear = 30 * n + j - 30

        val safeMonthIdx = ((hMonth - 1) % 12 + 12) % 12
        val mEn = hijriMonthNamesEn[safeMonthIdx]
        val mAr = hijriMonthNamesAr[safeMonthIdx]
        val mBn = hijriMonthNamesBn[safeMonthIdx]

        val formattedEn = "$hDay $mEn $hYear AH"
        val formattedAr = "$hDay $mAr $hYear هـ"

        return HijriDate(
            day = hDay,
            monthNameEn = mEn,
            monthNameAr = mAr,
            monthNameBn = mBn,
            year = hYear,
            formattedEn = formattedEn,
            formattedAr = formattedAr
        )
    }

    fun formatDate(timestamp: Long, pattern: String = "MMMM d, yyyy"): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun isSameDay(time1: Long, time2: Long): Boolean {
        val c1 = Calendar.getInstance().apply { timeInMillis = time1 }
        val c2 = Calendar.getInstance().apply { timeInMillis = time2 }
        return isSameDay(c1, c2)
    }

    fun isSameDayMonth(time1: Long, time2: Long): Boolean {
        val c1 = Calendar.getInstance().apply { timeInMillis = time1 }
        val c2 = Calendar.getInstance().apply { timeInMillis = time2 }
        return c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) &&
                c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH)
    }
}
