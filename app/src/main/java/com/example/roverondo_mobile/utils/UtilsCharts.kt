package com.example.roverondo_mobile.utils

import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DecimalFormat


object UtilsCharts {

    // Value formatters
    private class IntegerFormatter : ValueFormatter() {
        private val formatter = DecimalFormat("###,##0")
        override fun getBarLabel(barEntry: BarEntry): String = formatter.format(barEntry.y)
    }

    private class FloatFormatter : ValueFormatter() {
        override fun getBarLabel(barEntry: BarEntry): String = String.format("%.3f", barEntry.y)
    }

    private class StringFormatter(val xLabels: List<String>) : ValueFormatter() {
        override fun getFormattedValue(value: Float): String = xLabels[value.toInt()]
    }

    /*
    This function helps in creating line chart. On x axis there will always be only two labels:
    "Start" and "Finish".
     */
    fun setUpLineChart(
        chart: LineChart,
        data: List<Number>,
        color: Int
    ) {

        // Init X labels and Y Values
        val yValues = ArrayList<Entry>()
        val xLabels = ArrayList<String>()
        data.forEachIndexed { index, it ->
            yValues.add(Entry(index.toFloat(), it.toFloat()))
            xLabels.add(Consts.NONE)
        }
        xLabels[xLabels.size - 1] = "Finish"
        xLabels[0] = "Start"

        // Init Y options
        val set = LineDataSet(yValues, null).apply {
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.2F
            setDrawFilled(true)
            setDrawCircles(false)
            lineWidth = 2F
            circleRadius = 4F
            setCircleColor(color)
            highLightColor = color
            fillColor = color
            fillAlpha = 100
            this.color = color
            setDrawHorizontalHighlightIndicator(false)
            setFillFormatter { _, _ -> chart.axisLeft.axisMinimum }
        }

        // Set Y options
        val lineData = LineData(set)
        lineData.setValueTextSize(9F)
        lineData.setDrawValues(false)
        chart.data = lineData

        // Set Y formatter
        val yFormatter = when (data.first()::class.java) {
            Integer::class.java -> IntegerFormatter()
            else -> FloatFormatter()
        }
        lineData.setValueFormatter(yFormatter)

        // Set X options
        chart.axisRight.isEnabled = false
        chart.xAxis.setLabelCount(2, true)
        chart.xAxis.valueFormatter = StringFormatter(xLabels)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM

        // Set description and legend
        chart.description = Description().apply { text = "" }
        chart.legend.isEnabled = false

        // Make chart visible
        chart.isSelected = true
    }

    /*
    This function helps in creating bar chart. On x axis there will always be short names of last
    months from where data comes from.
     */
    fun <T : Number> setUpBarChart(
        chart: BarChart,
        data: List<T>,
        datesFrom: List<String> = ArrayList(),
        color: Int
    ) {

        // Init X labels and Y Values
        val xLabels = datesFrom.map { UtilsDates.timestampToMonthName(it) }
        val yValues = data.mapIndexed { index, it -> BarEntry(index.toFloat(), it.toFloat()) }

        // Set Y options
        val set = BarDataSet(yValues, null)
        set.color = color
        val barData = BarData(set)
        barData.setValueTextSize(9F)
        chart.data = barData
        chart.axisLeft.axisMinimum = 0F

        // Set Y formatter
        val yFormatter = when (data.first()::class.java) {
            Integer::class.java -> IntegerFormatter()
            else -> FloatFormatter()
        }
        barData.setValueFormatter(yFormatter)

        // Set X options
        chart.axisRight.isEnabled = false
        chart.xAxis.labelCount = datesFrom.size
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.valueFormatter = StringFormatter(xLabels)

        // Remove background vertical grid
        chart.xAxis.setDrawGridLines(false)

        // Set description and legend
        chart.description = Description().apply { text = "" }
        chart.legend.isEnabled = false

        // Make chart visible
        chart.isSelected = true
    }
}