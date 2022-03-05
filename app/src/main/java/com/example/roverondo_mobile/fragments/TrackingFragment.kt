package com.example.roverondo_mobile.fragments

import android.annotation.SuppressLint
import android.app.Service
import android.content.*
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleService
import com.example.roverondo_mobile.R
import com.example.roverondo_mobile.activities.GeneralActivity
import com.example.roverondo_mobile.api.ApiClient
import com.example.roverondo_mobile.services.TrackedRoute
import com.example.roverondo_mobile.models.Models
import com.example.roverondo_mobile.services.TrackingService
import com.example.roverondo_mobile.utils.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_tracking.*
import kotlinx.android.synthetic.main.fragment_user_profile.*
import kotlin.math.pow


class TrackingFragment : Fragment(R.layout.fragment_tracking), SensorEventListener {

    // Variables & consts
    private lateinit var thisActivity: GeneralActivity
    private var service: TrackingService? = null
    private var bound: Boolean = false
    private var map: GoogleMap? = null
    private var currentTrackedRoute: TrackedRoute? = null
    private var sensorManager: SensorManager? = null
    private var isBarometer: Boolean = false
    private var lastPressure: Double = Consts.SEA_LEVEL_PRESSURE
    private var plannedRoute: ArrayList<Models.Point>? = null
    private var postId: Int? = null
    var isTracking: Boolean = false

    /*
    This "constructor" initializes UI, map, sensors and listeners.
     */
    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        trackingMapView.onCreate(savedInstanceState)

        // Init variables
        thisActivity = activity as GeneralActivity
        thisActivity.invalidateOptionsMenu()
        postId = arguments?.getInt(Consts.ID)

        // Init UI Blur
        UtilsUI.addBlur(trackingStartStopBlurView, thisActivity)
        UtilsUI.addBlur(trackingInformationBlurView, thisActivity)

        // Init map and sensor
        initMap()
        initBarometer()

        // Start / stop button listeners
        trackingStartStopBlurView.setOnClickListener { startStop() }
    }

    /*
    This function initializes map. If there is route to follow, then that map is upgraded with
    polyline of that route to track.
     */
    @SuppressLint("MissingPermission")
    private fun initMap() {
        trackingMapView.getMapAsync {

            // Init
            map = it
            it.isMyLocationEnabled = true
            MapsInitializer.initialize(thisActivity)

            // Update polyroute (if possible)
            if (postId != null) {
                ApiClient.runRequestWithParser(
                    path = "api/plannedRoutePosts/$postId/plannedRoute",
                    method = "GET",
                    modelClass = Models.Post::class.java
                ) { post ->
                    plannedRoute = post.route?.route as ArrayList<Models.Point>?
                    thisActivity.runOnUiThread {
                        UtilsMaps.addRouteToMap(map!!, plannedRoute, color = UtilsMaps.FOLLOW_ROUTE_COLOR)
                        thisActivity.updateToolbar(getString(R.string.following_route))
                    }
                }
            }
        }
    }

    /*
    This function checks whether GPS is enabled. If it is not, then warning modal with warning
    appears in the middle of screen. Tracking service cannot be switched on when GPS is disabled.
     */
    private fun isGpsEnabled(): Boolean {
        val locationManager =
            thisActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gpsEnabled = false
        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
        }

        if (!gpsEnabled)
            UtilsAlerts.alertDialogGpsSettings("Please switch on GPS")

        return gpsEnabled
    }

    /*
    This function initializes barometer sensor. When smartphone does not have barometer, then
    equation for atmospheric pressure is used.
     */
    private fun initBarometer() {
        isBarometer =
            thisActivity.packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_BAROMETER)

        // Init hardware barometer if possible
        if (isBarometer) {
            sensorManager = thisActivity.getSystemService(Service.SENSOR_SERVICE) as SensorManager
            sensorManager!!.registerListener(
                this,
                sensorManager!!.getDefaultSensor(Sensor.TYPE_PRESSURE),
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    /*
    This function operates start / stop procedure. If tracking is enabled, then this is stop button,
    if tracking is off, then this is start button. Stopping switches off tracking service and it
    moves user to Post Workout activity. Starting initializes new route and switcehs on
    tracking service.
     */
    private fun startStop() {

        // Init
        val serviceIntent = Intent(thisActivity, TrackingService::class.java)

        // Set up starting / stopping service
        when (isTracking) {
            true -> {
                if (currentTrackedRoute?.speeds?.size!! > 1) {

                    // Update UI
                    trackingStartStopTextView.text =
                        thisActivity.getString(R.string.start_new_route)
                    thisActivity.enableNavigationDrawer()

                    // Stop service
                    serviceIntent.action = Consts.ACTION_STOP_SERVICE
                    thisActivity.startService(serviceIntent)
                    thisActivity.unbindService(connection)

                    // Go to Post Workout Fragment
                    thisActivity.openPostWorkout(currentTrackedRoute!!)
                    currentTrackedRoute = null
                    isTracking = false
                }
            }
            false -> {
                if (isGpsEnabled()) {

                    // Update UI
                    trackingStartStopTextView.text = thisActivity.getString(R.string.stop_route)
                    serviceIntent.action = Consts.ACTION_START_SERVICE
                    thisActivity.disableNavigationDrawer()

                    // Init
                    currentTrackedRoute = TrackedRoute(userId = thisActivity.currentUser.id)
                    isTracking = true

                    // Start service
                    thisActivity.startService(serviceIntent)
                    thisActivity.bindService(
                        serviceIntent,
                        connection,
                        LifecycleService.BIND_AUTO_CREATE
                    )
                }
            }
        }
    }

    /*
    This function updates map and and atmospheric pressure value.
     */
    @SuppressLint("SetTextI18n")
    fun updateMapAndData(location: Location): Pair<Long, String> {
        // Init elevation
        val elevation = location.altitude

        // Init pressure
        val newPressure: Double = when (isBarometer) {
            true -> {
                lastPressure
            }
            false -> {
                Consts.SEA_LEVEL_PRESSURE * (1 - 0.0065 * elevation / (Consts.DEFAULT_TEMPERATURE + 0.0065 * elevation + 273.15)).pow(
                    5.257
                )
            }
        }

        // Init distance
        var totalDistance = 0L

        // Update map route
        when (currentTrackedRoute?.lattitudes?.isEmpty()) {
            true -> {

                // First point on map ever
                currentTrackedRoute?.addPoint(location, newPressure, elevation)
                val loc = LatLng(location.latitude, location.longitude)
                UtilsMaps.addOneSegmentToMap(map!!, loc, loc)
            }
            false -> {

                // Init point before
                val latBefore = currentTrackedRoute?.lattitudes?.last()!!
                val lngBefore = currentTrackedRoute?.longtitudes?.last()!!
                val locBefore = LatLng(latBefore, lngBefore)

                // Init point after
                currentTrackedRoute?.addPoint(location, newPressure, elevation)
                val locAfter = LatLng(location.latitude, location.longitude)

                // Init distance
                for (i in 1 until currentTrackedRoute?.speeds?.size!!) {
                    val bufResults = FloatArray(1)
                    Location.distanceBetween(
                        currentTrackedRoute?.lattitudes!![i - 1],
                        currentTrackedRoute?.longtitudes!![i - 1],
                        currentTrackedRoute?.lattitudes!![i],
                        currentTrackedRoute?.longtitudes!![i],
                        bufResults
                    )
                    totalDistance += bufResults[0].toLong()
                }

                // Update map route
                UtilsMaps.addOneSegmentToMap(map!!, locBefore, locAfter)
            }
        }

        // Init time difference
        val timeDifference =
            currentTrackedRoute?.timestamps?.last()!! - currentTrackedRoute?.timestamps?.first()!!

        // Update time difference
        val parsedTime = UtilsDates.milisecondsToTime(timeDifference)
        trackingDurationDataTextView.text = parsedTime

        // Update total distance
        trackingDistanceDataTextView.text = UtilsUI.parseNullToKilometers(totalDistance)

        return Pair(totalDistance, parsedTime)
    }

    /*
    This function is run when barometer pressure is changed.
     */
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_PRESSURE) {
            val pressure = event.values[0]
            lastPressure = pressure.toDouble()
        }
    }

    /*
    This function is run when barometer accuracy is changed.
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    /*
    Way to use LocationService methods from outside.
     */
    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder: TrackingService.LocalBinder = service as TrackingService.LocalBinder
            this@TrackingFragment.service = binder.serverInstance
            bound = true

            this@TrackingFragment.service!!.trackingFragment = this@TrackingFragment
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bound = false
        }
    }

    /*
    When it is simple tracking -> this is "New route" fragment.
    When it is tracking with following route -> this is "Following route" fragment.
     */
    override fun onResume() {
        super.onResume()
        trackingMapView?.onResume()
        when (plannedRoute == null) {
            true -> thisActivity.updateToolbar(getString(R.string.new_route))
            false -> thisActivity.updateToolbar(getString(R.string.following_route))
        }
        thisActivity.updateNavigationCheckedItem(R.id.nav_new_route)
    }

    override fun onStart() {
        super.onStart()
        trackingMapView?.onStart()
    }

    override fun onPause() {
        super.onPause()
        trackingMapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        trackingMapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        trackingMapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        trackingMapView?.onSaveInstanceState(outState)
    }
}