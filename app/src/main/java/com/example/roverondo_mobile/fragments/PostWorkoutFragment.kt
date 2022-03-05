package com.example.roverondo_mobile.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.roverondo_mobile.R
import com.example.roverondo_mobile.activities.GeneralActivity
import com.example.roverondo_mobile.api.ApiClient
import com.example.roverondo_mobile.services.TrackedRoute
import com.example.roverondo_mobile.models.Models
import com.example.roverondo_mobile.utils.*
import com.example.roverondo_mobile.utils.UtilsUI.loadCircularImage
import com.google.android.gms.maps.MapsInitializer
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_post_workout.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class PostWorkoutFragment : Fragment(R.layout.fragment_post_workout) {

    // Variables & consts
    private lateinit var thisActivity: GeneralActivity
    private lateinit var trackedRoute: TrackedRoute

    /*
    This "constructor" initializes some post data, map UI and charts.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Init variables
        thisActivity = activity as GeneralActivity
        thisActivity.invalidateOptionsMenu()
        trackedRoute = arguments?.getSerializable(Consts.ROUTE)!! as TrackedRoute

        // Init map, UI and charts
        initMap()
        initListeners()
        initUI()
        initCharts()
    }

    /*
    This function initializes map in this fragment. Map is static and contiains visible route
    of points on it.
     */
    @SuppressLint("MissingPermission")
    private fun initMap() {

        // Set up
        postWorkoutMapView.onCreate(null)
        postWorkoutMapView.onResume()
        postWorkoutMapView.getMapAsync { map ->

            // Map init
            map.isMyLocationEnabled = true
            MapsInitializer.initialize(thisActivity)

            // Add route points to map
            val routePoints = ArrayList<Models.Point>()
            for (i in 0 until trackedRoute.speeds.size) {
                val bufPoint = Models.Point(
                    longitude = trackedRoute.longtitudes[i].toFloat(),
                    latitude = trackedRoute.lattitudes[i].toFloat(),
                    elevation = trackedRoute.elevations[i].toFloat(),
                    speed = trackedRoute.speeds[i].toFloat(),
                    pressure = trackedRoute.pressures[i].toFloat()
                )
                routePoints.add(bufPoint)
            }

            UtilsMaps.addRouteToMap(map, routePoints)
        }
    }

    /*
    This function initializes listeners on save / cancel buttons.
     */
    private fun initListeners() {
        // Save button
        postWorkoutSaveRelativeLayout.setOnClickListener {
            when (checkTextFields()) {
                true -> {
                    initPostWorkoutToApi()
                }
                false -> UtilsAlerts.snackbar("Cannot make workout post")
            }
        }

        // Cancel button
        postWorkoutCancelImageView.setOnClickListener {
            thisActivity.openWallFragment()
        }
    }

    /*
    This function initializes UI - changes data about workout.
     */
    @SuppressLint("SetTextI18n")
    private fun initUI() {

        // Blurs
        UtilsUI.addBlur(postWorkoutBackgroundBlurLayout, thisActivity)
        UtilsUI.addBlur(postWorkoutSpeedProfileChartBackgroundBlurView, thisActivity)
        UtilsUI.addBlur(postWorkoutElevationProfileChartBackgroundBlurView, thisActivity)

        // Data: Total duration
        val duration = trackedRoute.timestamps.last() - trackedRoute.timestamps.first()
        val parsedDuration = UtilsDates.milisecondsToTime(duration)
        postWorkoutTotalDurationDataTextView.text = parsedDuration

        // Data: Total distance
        var totalDistance = 0L
        for (i in 1 until trackedRoute.speeds.size) {
            val bufResults = FloatArray(1)
            Location.distanceBetween(
                trackedRoute.lattitudes[i - 1],
                trackedRoute.longtitudes[i - 1],
                trackedRoute.lattitudes[i],
                trackedRoute.longtitudes[i],
                bufResults
            )
            totalDistance += bufResults[0].toLong()
        }
        postWorkoutTotalDistanceDataTextView.text = UtilsUI.parseNullToKilometers(totalDistance)

        // Data: Start time
        postWorkoutStartTimeDataTextView.text =
            UtilsDates.milisecondsToLongDate(trackedRoute.timestamps.first())

        // Data: End time
        postWorkoutEndTimeDataTextView.text =
            UtilsDates.milisecondsToLongDate(trackedRoute.timestamps.last())

        // Data: Author image
        postWorkoutAuthorImageView.loadCircularImage(thisActivity.currentUser)

        // Data: Author text
        postWorkoutAuthorDataTextView.text = thisActivity.currentUser.nickname
    }

    /*
    This function initializes planned workout charts.
     */
    private fun initCharts() {

        // Init data
        val speeds = mutableListOf<Float>()
        val elevations = mutableListOf<Float>()
        val pressures = mutableListOf<Float>()

        for (i in 0 until trackedRoute.speeds.size) {
            speeds.add(trackedRoute.speeds[i].toFloat() * 3.6F)
            elevations.add(trackedRoute.elevations[i].toFloat())
            pressures.add(trackedRoute.pressures[i].toFloat())
        }

        // Speed profile cahrt
        UtilsCharts.setUpLineChart(
            chart = postWorkoutSpeedProfileLineChart,
            data = speeds,
            color = Color.RED
        )

        // Elevation profile chart
        UtilsCharts.setUpLineChart(
            chart = postWorkoutElevationProfileLineChart,
            data = elevations,
            color = Color.BLUE
        )

        // Pressure profile chart
        UtilsCharts.setUpLineChart(
            chart = postWorkoutPressureProfileLineChart,
            data = pressures,
            color = Color.GREEN
        )
    }

    private fun initPostWorkoutToApi() {

        // Init
        val points = ArrayList<Models.Point>()
        for (i in 0 until trackedRoute.speeds.size) {
            points.add(
                Models.Point(
                    longitude = trackedRoute.longtitudes[i].toFloat(),
                    latitude = trackedRoute.lattitudes[i].toFloat(),
                    elevation = trackedRoute.elevations[i].toFloat(),
                    speed = trackedRoute.speeds[i].toFloat(),
                    pressure = trackedRoute.pressures[i].toFloat(),
                    timestamp = UtilsDates.milisecondsToTimestamp(trackedRoute.timestamps[i])
                )
            )
        }

        val workoutPost = Models.WorkoutToPost(
            title = postWorkoutTitleTextInputEditText.text.toString(),
            description = postWorkoutDescriptionTextInputEditText.text.toString(),
            route = points,
        )

        // Create body
        val mediaType = "application/json".toMediaTypeOrNull()
        val bodyJson = Gson().toJson(workoutPost)
        val body = bodyJson.toRequestBody(mediaType)

        // Make request
        ApiClient.runRequestWithoutParser(
            path = "api/activityPosts/users/${thisActivity.currentUser.id}",
            method = "POST",
            body = body
        ) { thisActivity.runOnUiThread { thisActivity.openWallFragment() } }
    }

    /*
    This function checks text fields. These text fields cannot be empty.
     */
    private fun checkTextFields(): Boolean{

        // Init
        var status = true

        // Check title
        if (postWorkoutTitleTextInputEditText.text.toString().isEmpty()) {
            status = false
            postWorkoutTitleTextInputLayout.error = "Empty title!"
            postWorkoutTitleTextInputLayout.isErrorEnabled = true
            postWorkoutTitleTextInputLayout.setErrorIconDrawable(R.drawable.ic_error)
            postWorkoutTitleTextInputLayout.boxStrokeColor = Color.RED
        } else {
            postWorkoutTitleTextInputLayout.isErrorEnabled = false
            postWorkoutTitleTextInputLayout.boxStrokeColor =
                thisActivity.getColor(R.color.AppLightGray)
        }

        // Check description
        if (postWorkoutDescriptionTextInputEditText.text.toString().isEmpty()) {
            status = false
            postWorkoutDescriptionTextInputLayout.error = "Empty description!"
            postWorkoutDescriptionTextInputLayout.isErrorEnabled = true
            postWorkoutDescriptionTextInputLayout.setErrorIconDrawable(R.drawable.ic_error)
            postWorkoutDescriptionTextInputLayout.boxStrokeColor = Color.RED
        } else {
            postWorkoutDescriptionTextInputLayout.isErrorEnabled = false
            postWorkoutDescriptionTextInputLayout.boxStrokeColor =
                thisActivity.getColor(R.color.AppLightGray)
        }

        return status
    }

    /*
    This fragment should always change toolbar text to "Workout details".
     */
    override fun onResume() {
        super.onResume()
        thisActivity.updateToolbar(getString(R.string.workout_details))
    }
}