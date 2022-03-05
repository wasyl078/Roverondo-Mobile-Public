package com.example.roverondo_mobile.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import com.example.roverondo_mobile.R
import com.example.roverondo_mobile.activities.GeneralActivity
import com.example.roverondo_mobile.adapters.PointAdapter
import com.example.roverondo_mobile.api.ApiClient
import com.example.roverondo_mobile.models.Models
import com.example.roverondo_mobile.utils.Consts
import com.example.roverondo_mobile.utils.UtilsMaps
import com.example.roverondo_mobile.utils.UtilsUI
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_set_up_route.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class SetUpRouteFragment : Fragment(R.layout.fragment_set_up_route) {

    // Variables & consts
    private lateinit var thisActivity: GeneralActivity
    private var fragmentGoogleMap: GoogleMap? = null
    private var dialogGoogleMap: GoogleMap? = null
    private val polyRoute: PolylineOptions = PolylineOptions()
    private var distance: Double? = null
    private lateinit var adapter: PointAdapter
    private var dialogAddress: Address? = null

    /*
    This "constructor" initializes UI, map and listeners.
     */
    @SuppressLint("MissingPermission", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        thisActivity = activity as GeneralActivity
        thisActivity.invalidateOptionsMenu()
        setupMapView.onCreate(savedInstanceState)

        // Init map
        setupMapView.getMapAsync {
            fragmentGoogleMap = it
            it.isMyLocationEnabled = true
        }

        // Blur
        UtilsUI.addBlur(setupTopBlurView, thisActivity)
        UtilsUI.addBlur(setupAddPointBlurLayout, thisActivity)
        UtilsUI.addBlur(setupNumberOfPointsBlurLayout, thisActivity)

        // Init adapter, UI and listeners
        adapter = PointAdapter(thisActivity)
        setupListView.adapter = adapter
        initUi()
        initListeners()
    }

    /*
    This function initializes UI - it set ups number of points view and set ups "Create route" button.
     */
    @SuppressLint("SetTextI18n")
    private fun initUi() {
        setupNumberOfPointsTextView.text = thisActivity.getString(R.string.points_number) + " 0"
        setupCreateRouteRelativeLayout.isEnabled = false
        setupCreateRouteRelativeLayout.background =
            AppCompatResources.getDrawable(thisActivity, R.drawable.gradient_grey_capsule)
    }

    /*
    This method initializes listeners on "Add point" button and on "Create route" button. When
    user wants to add new point to route, then dialog window with draggable marker is opened.
     */
    private fun initListeners() {

        // Add point
        setupAddPointRelativeLayout.setOnClickListener { openNewPointDialogWindow() }

        // Create route post
        setupCreateRouteRelativeLayout.setOnClickListener {
            val points = polyRoute.points.map {
                Models.Point(
                    it.longitude.toFloat(),
                    it.latitude.toFloat(),
                    0F,
                    null,
                    null
                )
            } as ArrayList<Models.Point>

            ApiClient.runRequestOpenElevationExtended(points = points) { elevationObject ->
                for (i in elevationObject.results.indices)
                    points[i].elevation = elevationObject.results[i].elevation!!.toFloat()
                thisActivity.runOnUiThread {
                    thisActivity.openPostRouteFragment(
                        points,
                        distance!!
                    )
                }
            }
        }
    }

    /*
    This function creates dialog window with map and draggable marker. When there are no points
    in adapter, then marker is set to Wroclaw. When there is one point, then marker is set to that
    point. When there are more points, then a polyroute is created and marker is set to last point.
     */
    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun openNewPointDialogWindow() {

        // Init dialog
        val dialog = Dialog(thisActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_new_point)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        UtilsUI.addBlur(dialog.findViewById(R.id.dialogNewPointBackgroundBlurLayout), thisActivity)

        // Current point text
        val currentPointTextView =
            dialog.findViewById(R.id.dialogNewPointCurrentPointTextView) as TextView

        // Init map
        dialogAddress = null
        val dialogMap = dialog.findViewById(R.id.dialogNewPointMapView) as MapView
        dialogMap.onCreate(null)
        dialogMap.onResume()
        dialogMap.getMapAsync {
            initDialogMap(it)
            initDialogMapListeners(currentPointTextView)

        }

        // Add button
        val addButton = dialog.findViewById(R.id.dialogNewPointAddRelativeLayout) as RelativeLayout
        addButton.setOnClickListener {

            if (dialogAddress != null) {
                addPoint(dialogAddress!!)
                setupNumberOfPointsTextView.text =
                    thisActivity.getString(R.string.points_number) + " " + adapter.dataSource.size
            }

            dialogMap.onPause()
            dialogMap.onStop()
            dialogMap.onDestroy()
            dialog.dismiss()
        }

        // Cancel button
        val cancelButton =
            dialog.findViewById(R.id.dialogNewPointCancelRelativeLayout) as RelativeLayout
        cancelButton.setOnClickListener {
            dialogMap.onPause()
            dialogMap.onStop()
            dialogMap.onDestroy()
            dialog.dismiss()
        }

        // Set dialog window wider
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(Objects.requireNonNull(dialog.window)?.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        dialog.window!!.attributes = lp

        // Remove black dim on Androdi < 8.0
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            dialog.window!!.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        dialog.show()
    }

    /*
    This function is run when map is initialzied. When there is 0 / 1 point then marker is set to
    Wroclaw / actual point. When there are more points then ready poly route is added.
     */
    @SuppressLint("MissingPermission")
    private fun initDialogMap(it: GoogleMap) {

        // Init
        dialogGoogleMap = it
        it.isMyLocationEnabled = true

        // Create draggable marker
        val marker = MarkerOptions()
            .draggable(true)
            .title("Move this marker to next point")

        // Put marker on Wroclaw / on last point
        when (adapter.dataSource.isEmpty()) {
            true -> marker.position(Consts.WROCLAW)
            false -> {
                val point = adapter.dataSource.last()
                marker.position(LatLng(point.latitude.toDouble(), point.longitude.toDouble()))
            }
        }

        // Put line on map when there are at least two points
        if (adapter.dataSource.size > 1) {
            activity?.runOnUiThread {
                dialogGoogleMap!!.clear()
                dialogGoogleMap!!.addPolyline(polyRoute)
            }
        }

        // Add marker and update camera
        dialogGoogleMap!!.addMarker(marker)
        dialogGoogleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 12F))
        dialogGoogleMap!!.uiSettings.isZoomControlsEnabled = true
    }

    /*
    This function initalizes listener on dialog map -> it creates marker drag listener that
    operates when user is changing its position.
     */
    private fun initDialogMapListeners(currentPointTextView: TextView) {

        // Add drag listener
        dialogGoogleMap!!.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {

            // Unused methods
            override fun onMarkerDragStart(marker: Marker) {}
            override fun onMarkerDrag(marker: Marker) {}

            // When user puts down marker
            @SuppressLint("SetTextI18n")
            override fun onMarkerDragEnd(marker: Marker) {
                val location = marker.position
                val geocoder = Geocoder(context, Locale.getDefault())
                try {
                    // Get address
                    dialogAddress =
                        geocoder.getFromLocation(location.latitude, location.longitude, 1)[0]

                    // Update text and marker
                    val latStr = String.format("%.6f", dialogAddress!!.latitude)
                    val lngStr = String.format("%.6f", dialogAddress!!.longitude)
                    currentPointTextView.text = "New point: $latStr $lngStr"
                    marker.title = "Press add!"
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        })
    }


    /*
    This function is called when "Add point" (from dialog window) listener is triggered by user.
     */
    @SuppressLint("SetTextI18n")
    private fun addPoint(address: Address) {

        // Add point from location to adapter
        adapter.dataSource.add(
            Models.Point(
                longitude = address.longitude.toFloat(),
                latitude = address.latitude.toFloat(),
                elevation = 0F,
                speed = null,
                pressure = null,
                location = address.getAddressLine(0)
            )
        )
        adapter.notifyDataSetChanged()

        // Init
        val points = adapter.dataSource

        // Add route when there are at least two points
        if (points.size > 1) {

            // Init
            val previous = points[points.size - 2]
            val current = points.last()
            val latlngStart = LatLng(previous.latitude.toDouble(), previous.longitude.toDouble())
            val latlngEnd = LatLng(current.latitude.toDouble(), current.longitude.toDouble())

            // Makre request to Open Routes API
            ApiClient.runRequestOpenRoute(
                latlngStart = latlngStart,
                latlngEnd = latlngEnd
            ) { openRouteUpdate(it) }
        } else {

            // Add marker (when there is only one point)
            val newMarker = MarkerOptions()
                .position(LatLng(address.latitude, address.longitude))
                .title(address.getAddressLine(0))
            fragmentGoogleMap!!.addMarker(newMarker)
        }
    }

    /*
    This function calls Open Route API and creates poly route that visualizes route between last
    two points added to adapter.
     */
    private fun openRouteUpdate(result: Models.OpenRouteTrack) {

        // Init polyroute
        val routePoints = result.features[0].geometry.coordinates
        routePoints.forEach { point ->
            val bufLatLng = LatLng(point[1].toDouble(), point[0].toDouble())
            polyRoute.add(bufLatLng)
        }
        polyRoute.width(8F).color(UtilsMaps.ROUTE_COLOR).geodesic(true)

        activity?.runOnUiThread {

            // Add polyroute to map
            fragmentGoogleMap!!.clear()
            fragmentGoogleMap!!.addPolyline(polyRoute)

            // Init boundaries zoom
            val latLngs: ArrayList<LatLng> = ArrayList(polyRoute.points)
            val builder = LatLngBounds.Builder()
            for (startPoint in latLngs) builder.include(startPoint)
            val bounds = builder.build()

            // Zoom to polytrip
            fragmentGoogleMap!!.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20))
        }

        // Calculate route distance
        val latLngs: ArrayList<LatLng> = ArrayList(polyRoute.points)
        var totalDistance = 0L
        for (i in 1 until latLngs.size) {
            val bufResults = FloatArray(1)
            Location.distanceBetween(
                latLngs[i - 1].latitude,
                latLngs[i - 1].longitude,
                latLngs[i].latitude,
                latLngs[i].longitude,
                bufResults
            )
            totalDistance += bufResults[0].toLong()
        }

        // Update route distance on UI
        activity?.runOnUiThread {
            setupDistanceDataTextView.text = UtilsUI.parseNullToKilometers(totalDistance)
            distance = totalDistance.toDouble()
            setupCreateRouteRelativeLayout.isEnabled = true
            setupCreateRouteRelativeLayout.background = AppCompatResources.getDrawable(
                thisActivity,
                R.drawable.gradient_green_light_capsule
            )
        }
    }

    /*
    This fragment should always change toolbar text to "Set up route".
     */
    override fun onResume() {
        super.onResume()
        setupMapView?.onResume()
        thisActivity.updateToolbar(getString(R.string.set_up_route))
        thisActivity.updateNavigationCheckedItem(R.id.nav_set_up_route)
    }

    override fun onStart() {
        super.onStart()
        setupMapView?.onStart()
    }

    override fun onPause() {
        super.onPause()
        setupMapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        setupMapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        setupMapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        setupMapView?.onSaveInstanceState(outState)
    }
}