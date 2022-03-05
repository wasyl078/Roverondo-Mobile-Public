package com.example.roverondo_mobile.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.roverondo_mobile.R
import com.example.roverondo_mobile.activities.GeneralActivity
import com.example.roverondo_mobile.models.Models
import com.example.roverondo_mobile.utils.Consts
import com.example.roverondo_mobile.utils.UtilsCharts
import com.example.roverondo_mobile.utils.UtilsMaps
import com.example.roverondo_mobile.utils.UtilsUI
import com.example.roverondo_mobile.utils.UtilsUI.loadCircularImage
import com.google.android.gms.maps.MapsInitializer
import kotlinx.android.synthetic.main.fragment_details_route.*

class DetailsRouteFragment : Fragment(R.layout.fragment_details_route) {

    // Variables & consts
    private lateinit var thisActivity: GeneralActivity
    private lateinit var post: Models.Post

    /*
    This "constructor" initializes post data, map UI and charts.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Init variables
        thisActivity = activity as GeneralActivity
        thisActivity.invalidateOptionsMenu()
        post = arguments?.getSerializable(Consts.POST)!! as Models.Post

        // Init map, UI and charts
        initMap()
        initUI()
        initCharts()

        // Init listeners
        detailsRouteStartRelativeLayout.setOnClickListener { thisActivity.openTrackingFragment(post.id!!) }
        detailsRouteAuthorRelativeLayout.setOnClickListener { thisActivity.openAnyUserProfile(post.user.id) }
    }

    /*
    This function initializes map in this fragment. Map is static and contiains visible route
    of points on it.
     */
    private fun initMap() {

        // Set up
        detailsRouteMapView.onCreate(null)
        detailsRouteMapView.onResume()
        detailsRouteMapView.getMapAsync { map ->

            // Map init
            MapsInitializer.initialize(thisActivity)

            // Add route points to map
            UtilsMaps.addRouteToMap(map, post.plannedRoute?.route?.route)
        }
    }

    /*
    This function initializes UI - changes data about route.
     */
    private fun initUI() {

        // Init
        val route = post.plannedRoute!!.route

        // Blurs
        UtilsUI.addBlur(detailsRouteBackgroundBlurLayout, thisActivity)
        UtilsUI.addBlur(detailsRouteElevationProfileChartBackgroundBlurView, thisActivity)

        // Data: Total distance
        detailsRouteTotalDistanceDataTextView.text = UtilsUI.parseNullToKilometers(route.distance)

        // Data: Location
        detailsRouteLocationDataTextView.text = UtilsUI.parseNullString(route.location)
        detailsRouteLocationDataTextView.isSelected = true

        // Data: Author name
        detailsRouteAuthorDataTextView.text = UtilsUI.parseNullString(post.user.nickname)
        detailsRouteAuthorDataTextView.isSelected = true

        // Data: Author image
        detailsRouteAuthorImageView.loadCircularImage(post.user)

        // Data: Min altitude
        detailsRouteMinAltitudeDataTextView.text = UtilsUI.parseNullToMeters(route.minAltitude)

        // Data: Avg altitude
        detailsRouteAvgAltitudeDataTextView.text = UtilsUI.parseNullToMeters(route.avgAltitude)

        // Data: Max altitude
        detailsRouteMaxAltitudeDataTextView.text = UtilsUI.parseNullToMeters(route.maxAltitude)
    }


    /*
    This function initializes planned route chart.
     */
    private fun initCharts() {

        // Init data
        val elevations = post.plannedRoute?.route?.route?.map { it.elevation }!!

        // Elevation profile chart
        UtilsCharts.setUpLineChart(
            chart = detailsRouteElevationProfileLineChart,
            data = elevations,
            color = Color.BLUE
        )
    }

    /*
    This fragment should always change toolbar text to "Route details".
     */
    override fun onResume() {
        super.onResume()
        detailsRouteMapView?.onResume()
        thisActivity.updateToolbar(getString(R.string.route_details))
    }

    override fun onStart() {
        super.onStart()
        detailsRouteMapView?.onStart()
    }

    override fun onPause() {
        super.onPause()
        detailsRouteMapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        detailsRouteMapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        detailsRouteMapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        detailsRouteMapView?.onSaveInstanceState(outState)
    }
}