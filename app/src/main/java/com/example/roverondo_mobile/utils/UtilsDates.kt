package com.example.roverondo_mobile.utils

import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


object UtilsDates {

    // Consts
    private const val SHORT_MONTH_ONLY = "MMM"
    private const val DATE_SHORT_FORMAT = "yyyy-MM-dd"
    private const val DATE_LONG_FORMAT = "yyyy-MM-dd HH:mm"
    private const val TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    private const val TIMESTAMP_FORMAT_2 = "yyyy-MM-dd'T'HH:mm:ss"
    private const val TIMESTAMP_Z1_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    private const val TIMESTAMP_Z2_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    private const val TIMESTAMP_X_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX"
    private val LOCALE = Locale.US

    /*
    Changing timestamp to short date.
     */
    fun timestampToShortDate(timestamp: String): String {
        val date = timestampToDate(timestamp)
        val formatter = SimpleDateFormat(DATE_SHORT_FORMAT, LOCALE)
        return formatter.format(date)
    }

    /*
     Chanigng timestamp to long date.
     */
    fun timestampToLongDate(timestamp: String): String {
        val date = timestampToDate(timestamp)
        val formatter = SimpleDateFormat(DATE_LONG_FORMAT, LOCALE)
        return formatter.format(date)
    }

    /*
    Calculating difference between timestamps.
     */
    fun differenceBetweenTimestamps(startTimestamp: String, endTimestamp: String): String {
        val startDate = timestampToDate(startTimestamp)
        val endDate = timestampToDate(endTimestamp)
        val differenceInMilis = endDate.time - startDate.time
        return milisecondsToTime(differenceInMilis)
    }

    /*
    Chaning miliseconds to long date.
     */
    fun milisecondsToLongDate(miliseconds: Long): String {
        val formatter = SimpleDateFormat(DATE_LONG_FORMAT, LOCALE)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = miliseconds
        return formatter.format(calendar.time)
    }

    /*
    Changing miliseconds to time.
     */
    fun milisecondsToTime(miliseconds: Long): String {

        // Init
        val totalSeconds = (miliseconds / 1000).toDouble()
        val hours = (totalSeconds / 3600).toInt()
        val minutes = ((totalSeconds - hours * 3600) / 60).toInt()
        val seconds = (totalSeconds - hours * 3600 - minutes * 60).toInt()

        // Parse to string
        val secondsParsed = if (seconds < 10) "0$seconds" else seconds.toString()
        val minutesParsed = if (minutes < 10) "0$minutes" else minutes.toString()
        val hoursParsed = if (hours < 10) "0$hours" else hours.toString()

        // Return joined
        return "$hoursParsed:$minutesParsed:$secondsParsed"
    }

    /*
     Changing milisecodns to timestamp.
     */
    fun milisecondsToTimestamp(miliseconds: Long): String {
        val formatter = SimpleDateFormat(TIMESTAMP_Z1_FORMAT, LOCALE)
        val date = Date(miliseconds)
        return formatter.format(date)
    }

    /*
     Checking whether text is with datetime format.
     */
    fun isStringDatetime(text: String): Boolean {
        var status = true
        val parser = SimpleDateFormat(DATE_LONG_FORMAT, LOCALE)
        try {
            parser.parse(text)
        } catch (e: Exception) {
            status = false
        }
        if (text.length != 16)
            status = false
        return status
    }

    /*
    This function takes long date string and changes it to timestamp
     */
    fun longDateToTimestamp(longDate: String): String{
        val parser = SimpleDateFormat(DATE_LONG_FORMAT, LOCALE)
        val formatter = SimpleDateFormat(TIMESTAMP_Z1_FORMAT, LOCALE)
        val date = parser.parse(longDate)!!
        return formatter.format(date)
    }

    /*
    This function takes date string and extracts month from it. Month is represented as month's name.
     */
    fun timestampToMonthName(timestamp: String): String{
        val date = timestampToDate(timestamp)
        val monthFormatter = SimpleDateFormat(SHORT_MONTH_ONLY, LOCALE)
        return monthFormatter.format(date)
    }

    /*
    This function returns dates from last 12 months.
     */
    fun getLast12MonthsDates(): ArrayList<String>{

        // Init
        val last12Months: ArrayList<String> = ArrayList()
        val formatter = SimpleDateFormat(DATE_SHORT_FORMAT, LOCALE)
        val todayTimestamp = formatter.format(Calendar.getInstance().time)
        val calendar = Calendar.getInstance()
        calendar.time = formatter.parse(todayTimestamp)!!

        // Calculate last 12 months
        for (i in 1..12) {
            last12Months.add(formatter.format(calendar.time))
            calendar.add(Calendar.MONTH, -1)
        }
        last12Months.reverse()

        // Return dates from last 12 months
        return last12Months
    }

    /*
    Parsing date with unknown timestamp format to date.
     */
    private fun timestampToDate(timestamp: String): Date {

        // Init
        val parserX = SimpleDateFormat(TIMESTAMP_X_FORMAT, LOCALE)
        val parserZ1 = SimpleDateFormat(TIMESTAMP_Z1_FORMAT, LOCALE)
        val parserZ2 = SimpleDateFormat(TIMESTAMP_Z2_FORMAT, LOCALE)
        val parser = SimpleDateFormat(TIMESTAMP_FORMAT, LOCALE)
        val parser2 = SimpleDateFormat(TIMESTAMP_FORMAT_2, LOCALE)
        val parserSimple = SimpleDateFormat(DATE_SHORT_FORMAT, LOCALE)

        // Parse
        return try {
            parserX.parse(timestamp)!!
        } catch (e: Exception) {
            try {
                parserZ1.parse(timestamp)!!
            } catch (e: Exception) {
                try{
                    parserZ2.parse(timestamp)!!
                } catch (e:java.lang.Exception){
                    try{
                        parser.parse(timestamp)!!
                    } catch (e:java.lang.Exception){
                        try{
                            parser2.parse(timestamp)!!
                        } catch (e:java.lang.Exception){
                            parserSimple.parse(timestamp)!!
                        }
                    }
                }
            }
        }
    }
}