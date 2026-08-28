package com.example.util

import java.time.LocalDate
import java.time.ZoneId

object JalaliCalendar {

    data class JalaliDate(val year: Int, val month: Int, val day: Int) {
        override fun toString(): String = String.format("%04d/%02d/%02d", year, month, day)
        fun formatLong(): String = "$day ${getJalaliMonthName(month)} $year"
    }

    data class GregorianDate(val year: Int, val month: Int, val day: Int) {
        fun toLocalDate(): LocalDate = LocalDate.of(year, month, day)
    }

    private val MONTH_NAMES_FA = arrayOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    private val MONTH_NAMES_EN = arrayOf(
        "Farvardin", "Ordibehesht", "Khordad",
        "Tir", "Mordad", "Shahrivar",
        "Mehr", "Aban", "Azar",
        "Dey", "Bahman", "Esfand"
    )

    fun getJalaliMonthName(month: Int): String {
        val idx = month - 1
        if (idx in 0..11) {
            return "${MONTH_NAMES_EN[idx]} (${MONTH_NAMES_FA[idx]})"
        }
        return "Unknown"
    }

    fun getJalaliMonthNameFa(month: Int): String {
        val idx = month - 1
        if (idx in 0..11) {
            return MONTH_NAMES_FA[idx]
        }
        return "نامشخص"
    }

    fun getJalaliMonthNameEn(month: Int): String {
        val idx = month - 1
        if (idx in 0..11) {
            return MONTH_NAMES_EN[idx]
        }
        return "Unknown"
    }

    fun currentJalaliDate(): JalaliDate {
        val today = LocalDate.now(ZoneId.of("Asia/Tehran"))
        return fromLocalDate(today)
    }

    fun fromLocalDate(localDate: LocalDate): JalaliDate {
        return gregorianToJalali(localDate.year, localDate.monthValue, localDate.dayOfMonth)
    }

    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): JalaliDate {
        val gDaysInMonth = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

        val gy2 = gy - 1600
        val gm2 = gm - 1
        val gd2 = gd - 1

        var gDayNo = 365 * gy2 + (gy2 + 4) / 4 - (gy2 + 100) / 100 + (gy2 + 400) / 400
        for (i in 0 until gm2) {
            gDayNo += gDaysInMonth[i + 1]
        }
        if (gm2 > 1 && ((gy2 % 4 == 0 && gy2 % 100 != 0) || gy2 % 400 == 0)) {
            gDayNo++
        }
        gDayNo += gd2

        var jDayNo = gDayNo - 79

        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        var jm = 0
        for (i in 0..11) {
            val days = if (i < 6) 31 else if (i < 11) 30 else if ((jy % 33) in intArrayOf(1, 5, 9, 13, 17, 22, 26, 30)) 30 else 29
            if (jDayNo < days) {
                jm = i + 1
                break
            }
            jDayNo -= days
        }
        val jd = jDayNo + 1

        return JalaliDate(jy, jm, jd)
    }

    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): GregorianDate {
        val gDaysInMonth = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

        val jy2 = jy - 979
        val jm2 = jm - 1
        val jd2 = jd - 1

        var jDayNo = 365 * jy2 + (jy2 / 33) * 8 + (jy2 % 33 + 3) / 4
        for (i in 0 until jm2) {
            jDayNo += if (i < 6) 31 else 30
        }
        jDayNo += jd2

        var gDayNo = jDayNo + 79

        var gy = 1600 + 400 * (gDayNo / 146097)
        gDayNo %= 146097

        var leap = true
        if (gDayNo >= 36525) {
            gDayNo--
            gy += 100 * (gDayNo / 36524)
            gDayNo %= 36524

            if (gDayNo >= 365) {
                gDayNo++
            } else {
                leap = false
            }
        }

        gy += 4 * (gDayNo / 1461)
        gDayNo %= 1461

        if (gDayNo >= 366) {
            leap = false
            gDayNo--
            gy += gDayNo / 365
            gDayNo %= 365
        }

        var i = 0
        while (i < 12) {
            var mDays = gDaysInMonth[i + 1]
            if (i == 1 && leap) {
                mDays++
            }
            if (gDayNo < mDays) {
                break
            }
            gDayNo -= mDays
            i++
        }
        val gm = i + 1
        val gd = gDayNo + 1

        return GregorianDate(gy, gm, gd)
    }
}
