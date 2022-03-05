package com.example.roverondo_mobile.utils

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

object UtilsPickers {

    /*
    This class is used to open Time Picker. That is a modal dialog window with ready-to-use UI
    elements that user can use to choose hour and minute.
     */
    class TimePickerFragment(
        val onResult: ((String, String) -> Unit)
    )  : DialogFragment(), TimePickerDialog.OnTimeSetListener {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
        }

        override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
            val minutesParsed = if (minute < 10) "0$minute" else minute.toString()
            val hoursParsed = if (hourOfDay < 10) "0$hourOfDay" else hourOfDay.toString()
            onResult(hoursParsed, minutesParsed)
        }
    }

    /*
    This class is used to open Date Picker. That is a modal dialog window with ready-to-use UI
    elements that user can use to choose year, month and day.
     */
    class DatePickerFragment(
        val onResult: ((String, String, String) -> Unit)
    ) : DialogFragment(), DatePickerDialog.OnDateSetListener {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            return DatePickerDialog(requireContext(), this, year, month, day)
        }

        override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
            val monthFix = month + 1
            val monthParsed = if (monthFix < 10) "0$monthFix" else monthFix.toString()
            val dayParsed = if (day < 10) "0$day" else day.toString()
            onResult(year.toString(), monthParsed, dayParsed)
        }
    }
}