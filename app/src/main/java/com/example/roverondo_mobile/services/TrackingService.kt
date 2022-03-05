package com.example.roverondo_mobile.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.example.roverondo_mobile.R
import com.example.roverondo_mobile.activities.GeneralActivity
import com.example.roverondo_mobile.fragments.TrackingFragment
import com.example.roverondo_mobile.utils.Consts
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult


class TrackingService : LifecycleService() {

    // Variables & consts
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var notificationBuilder: NotificationCompat.Builder? = null
    var trackingFragment: TrackingFragment? = null

    companion object {
        var LOCATION_PRIORITY: Int = LocationRequest.PRIORITY_HIGH_ACCURACY
    }


    /*
    This "constructor" initializes location provider that is later used to receive GPS location
    from staelites or from near transmitters.
     */
    @SuppressLint("VisibleForTests")
    override fun onCreate() {
        super.onCreate()
        fusedLocationProviderClient = FusedLocationProviderClient(this)
    }

    /*
    This "late constructor" receives actions from fragment and according to these actions it
    either starts or stops tracking users route.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Consts.ACTION_START_SERVICE -> {
                startForegroundService()
                updateLocationTracker(true)
            }
            Consts.ACTION_STOP_SERVICE -> {
                updateLocationTracker(false)
                stopForeground(true)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /*
    This function updates location tracker -> it switches it on or off.
     */
    @SuppressLint("MissingPermission")
    private fun updateLocationTracker(turnTracking: Boolean) {
        when (turnTracking) {
            false -> fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            true -> {
                val request = LocationRequest.create().apply {
                    interval = Consts.LOCATION_UPDATE_INTERVAL
                    fastestInterval = Consts.FASTEST_LOCATION_INTERVAL
                    priority = LOCATION_PRIORITY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        }
    }

    /*
    This function is automaticcaly run whether device receives new location signal.
     */
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)

            // Update route
            val newData = trackingFragment?.updateMapAndData(result.lastLocation)

            // Update notification
            notificationBuilder?.setContentText("${newData!!.second} | ${newData.first} m")
            val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notifManager.notify(Consts.NOTIFICATION_ID, notificationBuilder!!.build())
        }
    }

    /*
    This function is called when user in Tracking Fragment wants to start totally new route.
    When that happens, whole service is set up and started.
     */
    private fun startForegroundService() {
        val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since Androids 8.0 there have to be notification when app is running in background
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            createNotificationChannel(notifManager)

        // Creating notification intent
        val notificationIntent = Intent(this, GeneralActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        // Creating intent for user to click it and automaticcaly go back to Tracking Fragment
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Creating notification
        notificationBuilder = NotificationCompat.Builder(this, Consts.NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_bike)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("00:00:00 | 0 m")
            .setContentIntent(pendingIntent)

        // Starting service
        startForeground(Consts.NOTIFICATION_ID, notificationBuilder!!.build())
    }


    /*
    This function runs only on Android 8.0+. It creates notification channel.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            Consts.NOTIFICATION_CHANNEL_ID,
            Consts.NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    // Way to use methods from this class outside
    private val binder: IBinder = LocalBinder(this)

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    class LocalBinder(val serverInstance: TrackingService) : Binder()
}