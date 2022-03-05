package com.example.roverondo_mobile.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.roverondo_mobile.R
import com.example.roverondo_mobile.activities.GeneralActivity
import com.example.roverondo_mobile.models.Models
import com.example.roverondo_mobile.utils.*
import com.example.roverondo_mobile.utils.UtilsUI.loadCircularImage
import com.google.android.gms.maps.MapsInitializer
import kotlinx.android.synthetic.main.fragment_details_workout.*

class DetailsWorkoutFragment : Fragment(R.layout.fragment_details_workout) {

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
    }

    /*
    This function initializes map in this fragment. Map is static and contiains visible route
    of points on it.
     */
    private fun initMap() {

        // Set up
        detailsWorkoutMapView.onCreate(null)
        detailsWorkoutMapView.onResume()
        detailsWorkoutMapView.getMapAsync { map ->

            // Map init
            MapsInitializer.initialize(thisActivity)

            // Add route points to map
            UtilsMaps.addRouteToMap(map, post.workout?.route?.route)
        }
    }

    /*
    This function initializes UI - changes data about workout.
     */
    private fun initUI() {

        // Get workout
        val workout = post.workout!!

        // Blurs
        UtilsUI.addBlur(detailsWorkoutBackgroundBlurLayout, thisActivity)
        UtilsUI.addBlur(detailsWorkoutSpeedProfileChartBackgroundBlurView, thisActivity)
        UtilsUI.addBlur(detailsWorkoutElevationProfileChartBackgroundBlurView, thisActivity)

        // Data: Total duration
        val duration = if (workout.startTime != null && workout.endTime != null) {
            UtilsDates.differenceBetweenTimestamps(workout.startTime, workout.endTime)
        } else {
            Consts.NONE
        }

        detailsWorkoutTotalDurationDataTextView.text = duration

        // Data: Total distance
        detailsWorkoutTotalDistanceDataTextView.text =
            UtilsUI.parseNullToKilometers(workout.route.distance)

        // Data: Start time
        detailsWorkoutStartTimeDataTextView.text =
            UtilsUI.parseNullDatetimeString(workout.startTime)
        detailsWorkoutStartTimeDataTextView.isSelected = true

        // Data: End time
        detailsWorkoutEndTimeDataTextView.text = UtilsUI.parseNullDatetimeString(workout.endTime)
        detailsWorkoutEndTimeDataTextView.isSelected = true

        // Data: Average speed
        detailsWorkoutAverageSpeedDataTextView.text =
            UtilsUI.parseNullToKilometersPerMeters(workout.averageSpeed)

        // Data: Max speed
        detailsWorkoutMaxSpeedDataTextView.text =
            UtilsUI.parseNullToKilometersPerMeters(workout.maxSpeed)

        // Data: Min pressure
        detailsWorkoutMinPressureDataTextView.text =
            UtilsUI.parseNullString(workout.minAtmosphericPressure, extraString = "hPa")

        // Data: Avg pressure
        detailsWorkoutAvgPressureDataTextView.text =
            UtilsUI.parseNullString(workout.avgAtmosphericPressure, extraString = "hPa")

        // Data: Max pressure
        detailsWorkoutMaxPressureDataTextView.text =
            UtilsUI.parseNullString(workout.maxAtmosphericPressure, extraString = "hPa")

        // Data: Min altitude
        detailsWorkoutMinAltitudeDataTextView.text =
            UtilsUI.parseNullToMeters(workout.route.minAltitude)

        // Data: Avg altitude
        detailsWorkoutAvgAltitudeDataTextView.text =
            UtilsUI.parseNullToMeters(workout.route.avgAltitude)

        // Data: Max altitude
        detailsWorkoutMaxAltitudeDataTextView.text =
            UtilsUI.parseNullToMeters(workout.route.maxAltitude)

        // Data: Location
        detailsWorkoutLocationDataTextView.text = UtilsUI.parseNullString(workout.route.location)
        detailsWorkoutLocationDataTextView.isSelected = true

        // Data: Author name
        detailsWorkoutAuthorDataTextView.text = UtilsUI.parseNullString(post.user.nickname)
        detailsWorkoutAuthorDataTextView.isSelected = true

        // Data: Author image
        detailsWorkoutAuthorImageView.loadCircularImage(post.user)

        // Listener: Author
        detailsWorkoutAuthorRelativeLayout.setOnClickListener { thisActivity.openAnyUserProfile(post.user.id) }
    }

    /*
    This function initializes planned workout charts.
     */
    private fun initCharts() {

        // Init data
        val speeds = mutableListOf<Float>()
        val elevations = mutableListOf<Float>()
        val pressures = mutableListOf<Float>()
        post.workout?.route?.route?.forEach { it ->
            elevations.add(it.elevation)
            speeds.add(it.speed!! * 3.6F)

            // There may be no pressure data
            if (it.pressure != null)
                pressures.add(it.pressure)
        }

        // Speed profile cahrt
        UtilsCharts.setUpLineChart(
            chart = detailsWorkoutSpeedProfileLineChart,
            data = speeds,
            color = Color.RED
        )

        // Elevation profile chart
        UtilsCharts.setUpLineChart(
            chart = detailsWorkoutElevationProfileLineChart,
            data = elevations,
            color = Color.BLUE
        )

        // Pressure profile chart
        when (pressures.isNotEmpty()) {
            true ->
                UtilsCharts.setUpLineChart(
                    chart = detailsWorkoutPressureProfileLineChart,
                    data = pressures,
                    color = Color.GREEN
                )
            false ->
                detailsWorkoutPressureProfileLineChart.setNoDataText(getString(R.string.no_pressure_data))
        }
    }

    /*
    This fragment should always change toolbar text to "Workout details".
     */
    override fun onResume() {
        super.onResume()
        thisActivity.updateToolbar(getString(R.string.workout_details))
        detailsWorkoutMapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        detailsWorkoutMapView?.onStart()
    }

    override fun onPause() {
        super.onPause()
        detailsWorkoutMapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        detailsWorkoutMapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        detailsWorkoutMapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        detailsWorkoutMapView?.onSaveInstanceState(outState)
    }
}