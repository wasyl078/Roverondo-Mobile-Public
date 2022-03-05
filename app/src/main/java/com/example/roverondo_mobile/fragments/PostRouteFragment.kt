package com.example.roverondo_mobile.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.roverondo_mobile.R
import com.example.roverondo_mobile.activities.GeneralActivity
import com.example.roverondo_mobile.api.ApiClient
import com.example.roverondo_mobile.models.Models
import com.example.roverondo_mobile.utils.*
import com.example.roverondo_mobile.utils.UtilsUI.loadCircularImage
import com.google.android.gms.maps.MapsInitializer
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_post_route.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class PostRouteFragment : Fragment(R.layout.fragment_post_route) {

    // Variables & consts
    private lateinit var thisActivity: GeneralActivity
    private lateinit var points: ArrayList<Models.Point>
    private var distance: Double? = null

    /*
    This "constructor" initializes some post data, map UI and charts.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Init variables
        thisActivity = activity as GeneralActivity
        thisActivity.invalidateOptionsMenu()
        points = arguments?.getSerializable(Consts.POINTS)!! as ArrayList<Models.Point>
        distance = arguments?.getDouble(Consts.DISTANCE)!!

        // Init map, UI and charts
        initMap()
        initListeners()
        initUI()
        initChart()
    }

    /*
    This function initializes map in this fragment. Map is static and contiains visible route
    of points on it.
     */
    @SuppressLint("MissingPermission")
    private fun initMap() {

        // Set up
        postRouteMapView.onCreate(null)
        postRouteMapView.onResume()
        postRouteMapView.getMapAsync { map ->

            // Map init
            map.isMyLocationEnabled = true
            MapsInitializer.initialize(thisActivity)

            // Add route points to map
            UtilsMaps.addRouteToMap(map, points)
        }
    }

    /*
    This function initializes listeners on save / cancel buttons.
     */
    private fun initListeners() {
        // Save button
        postRouteSaveRelativeLayout.setOnClickListener {

            when (checkTextFields()) {
                true -> {
                    initPostPlannedRouteToApi()
                }
                false -> UtilsAlerts.snackbar("Cannot make route post")
            }
        }

        // Cancel button
        postRouteCancelImageView.setOnClickListener {
            thisActivity.openWallFragment()
        }
    }

    /*
    This function initializes UI - changes data about route.
     */
    @SuppressLint("SetTextI18n")
    private fun initUI() {

        // Blurs
        UtilsUI.addBlur(postRouteBackgroundBlurLayout, thisActivity)
        UtilsUI.addBlur(postRouteElevationProfileChartBackgroundBlurView, thisActivity)

        // Data: Author image
        postRouteAuthorImageView.loadCircularImage(thisActivity.currentUser)

        // Data: Author text
        postRouteAuthorDataTextView.text = thisActivity.currentUser.nickname

        // Data: Distance
        postRouteTotalDistanceDataTextView.text = UtilsUI.parseNullToKilometers(distance)
    }

    /*
    This function initializes route charts.
     */
    private fun initChart() {

        // Init data
        val elevations = points.map { it.elevation }

        // Elevation profile chart
        UtilsCharts.setUpLineChart(
            chart = postRouteElevationProfileLineChart,
            data = elevations,
            color = Color.BLUE
        )
    }

    /*
    This function checks text fields. These text fields cannot be empty.
     */
    private fun checkTextFields(): Boolean {

        // Init
        var status = true

        // Check title
        if (postRouteTitleTextInputEditText.text.toString().isEmpty()) {
            status = false
            postRouteTitleTextInputLayout.error = "Empty title!"
            postRouteTitleTextInputLayout.isErrorEnabled = true
            postRouteTitleTextInputLayout.setErrorIconDrawable(R.drawable.ic_error)
            postRouteTitleTextInputLayout.boxStrokeColor = Color.RED
        } else {
            postRouteTitleTextInputLayout.isErrorEnabled = false
            postRouteTitleTextInputLayout.boxStrokeColor =
                thisActivity.getColor(R.color.AppLightGray)
        }

        // Check description
        if (postRouteDescriptionTextInputEditText.text.toString().isEmpty()) {
            status = false
            postRouteDescriptionTextInputLayout.error = "Empty description!"
            postRouteDescriptionTextInputLayout.isErrorEnabled = true
            postRouteDescriptionTextInputLayout.setErrorIconDrawable(R.drawable.ic_error)
            postRouteDescriptionTextInputLayout.boxStrokeColor = Color.RED
        } else {
            postRouteDescriptionTextInputLayout.isErrorEnabled = false
            postRouteDescriptionTextInputLayout.boxStrokeColor =
                thisActivity.getColor(R.color.AppLightGray)
        }

        return status
    }

    /*
    This function making request to API with planned route.
     */
    private fun initPostPlannedRouteToApi() {

        // Init
        val plannedRoute = Models.PlannedRouteToPost(
            title = postRouteTitleTextInputEditText.text.toString(),
            description = postRouteDescriptionTextInputEditText.text.toString(),
            points = points,
        )

        // Create body -> Planned Route
        val mediaType = "application/json".toMediaTypeOrNull()
        val bodyJson = Gson().toJson(plannedRoute)

        val body = bodyJson.toRequestBody(mediaType)

        // Make request -> Planned Route
        ApiClient.runRequestWithParser(
            path = "api/plannedRoutes/users/${thisActivity.currentUser.id}",
            method = "POST",
            body = body,
            modelClass = Models.PlannedRouteToPost::class.java
        ) { thisActivity.runOnUiThread { initPostPlannedRoutePostToApi(it.id!!) } }
    }

    /*
    This function making request to API with planned route post.
     */
    private fun initPostPlannedRoutePostToApi(plannedRouteId: Int) {

        // Init
        val plannedRoutePost = Models.PlannedRouteToPostPost(
            title = postRouteTitleTextInputEditText.text.toString(),
            description = postRouteDescriptionTextInputEditText.text.toString()
        )

        // Create body -> Planned Route
        val mediaType = "application/json".toMediaTypeOrNull()
        val bodyJson = Gson().toJson(plannedRoutePost)
        val body = bodyJson.toRequestBody(mediaType)

        // Make request -> Planned Route
        ApiClient.runRequestWithoutParser(
            path = "api/plannedRoutePosts/users/${thisActivity.currentUser.id}/plannedRoutes/${plannedRouteId}",
            method = "POST",
            body = body
        ) { thisActivity.runOnUiThread { thisActivity.openWallFragment() } }
    }

    /*
    This fragment should always change toolbar text to "Route details".
     */
    override fun onResume() {
        super.onResume()
        thisActivity.updateToolbar(getString(R.string.post_route))
    }
}