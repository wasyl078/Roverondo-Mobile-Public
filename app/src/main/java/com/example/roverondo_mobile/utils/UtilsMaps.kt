package com.example.roverondo_mobile.utils

import android.graphics.Color
import com.example.roverondo_mobile.models.Models
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions


object UtilsMaps {

    // Variables & consts
    var ROUTE_COLOR = Color.RED
    var FOLLOW_ROUTE_COLOR = Color.BLUE

    /*
    This function helps in creating route of points on map. Map is always static, and camera
    is always focused on that route.
     */
    fun addRouteToMap(
        map: GoogleMap,
        routePoints: List<Models.Point>?,
        width: Float = 8F,
        color: Int = ROUTE_COLOR,
        padding: Int = 20
    ) {
        // Init
        map.clear()
        val polyRoute = PolylineOptions()
        routePoints?.forEach {
            val bufLatLng = LatLng(it.latitude.toDouble(), it.longitude.toDouble())
            polyRoute.add(bufLatLng)
        }
        polyRoute.width(width).color(color).geodesic(true)

        // Empty route
        if (routePoints == null)
            return

        // Add points
        map.addPolyline(polyRoute)

        // Add bounds
        val latLngs: ArrayList<LatLng> = ArrayList(polyRoute.points)
        val builder = LatLngBounds.Builder()
        for (startPoint in latLngs) builder.include(startPoint)
        val bounds = builder.build()

        // Zoom to polytrip
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
    }

    /*
    This function helps in creating route of points on map during tracking workout.
    Map is animated and per call is added only one segment.
     */
    fun addOneSegmentToMap(
        map: GoogleMap,
        loc1: LatLng,
        loc2: LatLng,
        width: Float = 8F,
        color: Int = ROUTE_COLOR,
        zoom: Float = 18.0f
    ) {
        // Init
        val polySegment = PolylineOptions()
        polySegment.add(loc1)
        polySegment.add(loc2)
        polySegment.width(width).color(color).geodesic(true)

        // Add to map
        map.addPolyline(polySegment)

        // Zoom to that segment
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(loc2, zoom))
    }
}
