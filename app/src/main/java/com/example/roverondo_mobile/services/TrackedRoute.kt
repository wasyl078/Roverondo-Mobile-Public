package com.example.roverondo_mobile.services

import android.location.Location
import java.io.Serializable


class TrackedRoute(
    val userId: Int,
    val id: Int = System.currentTimeMillis().hashCode(),
    val lattitudes: ArrayList<Double> = ArrayList(),
    val longtitudes: ArrayList<Double> = ArrayList(),
    val speeds: ArrayList<Double> = ArrayList(),
    val pressures: ArrayList<Double> = ArrayList(),
    val elevations: ArrayList<Double> = ArrayList(),
    val timestamps: ArrayList<Long> = ArrayList()
) : Serializable {

    fun addPoint(location: Location, pressure: Double, elevation: Double) {
        lattitudes.add(location.latitude)
        longtitudes.add(location.longitude)
        speeds.add(location.speed.toDouble())
        pressures.add(pressure)
        elevations.add(elevation)
        timestamps.add(System.currentTimeMillis())
    }
}