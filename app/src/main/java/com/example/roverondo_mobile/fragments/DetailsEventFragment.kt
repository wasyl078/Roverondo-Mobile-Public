package com.example.roverondo_mobile.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import com.example.roverondo_mobile.R
import com.example.roverondo_mobile.activities.GeneralActivity
import com.example.roverondo_mobile.adapters.UserAdapter
import com.example.roverondo_mobile.api.ApiClient
import com.example.roverondo_mobile.models.Models
import com.example.roverondo_mobile.utils.Consts
import com.example.roverondo_mobile.utils.UtilsCharts
import com.example.roverondo_mobile.utils.UtilsMaps
import com.example.roverondo_mobile.utils.UtilsUI
import com.example.roverondo_mobile.utils.UtilsUI.loadCircularImage
import com.google.android.gms.maps.MapsInitializer
import kotlinx.android.synthetic.main.fragment_details_event.*

class DetailsEventFragment : Fragment(R.layout.fragment_details_event) {

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

        // Init map, UI, charts and listeners
        initMap()
        initUI()
        initCharts()
        initListeners()
    }

    /*
    This function initializes map in this fragment. Map is static and contiains visible route
    of points on it.
     */
    private fun initMap() {

        // Set up
        detailsEventMapView.onCreate(null)
        detailsEventMapView.onResume()
        detailsEventMapView.getMapAsync { map ->

            // Map init
            MapsInitializer.initialize(thisActivity)

            // Add route points to map
            UtilsMaps.addRouteToMap(map, post.plannedRoute?.route?.route)
        }
    }

    /*
    This function initializes UI - changes data about workout.
     */
    private fun initUI() {

        // Init
        val route = post.plannedRoute!!.route
        val dateAndTime = UtilsUI.parseNullDatetimeString(post.startsAt).split(" ")

        // Blurs
        UtilsUI.addBlur(detailsEventBackgroundBlurLayout, thisActivity)
        UtilsUI.addBlur(detailsEventElevationProfileChartBackgroundBlurView, thisActivity)

        // Data: Start time
        detailsEventStartTimeDataTextView.text = dateAndTime.last()

        // Data: Start date
        detailsEventStartDateDataTextView.text = dateAndTime.first()

        // Data: Total distance
        detailsEventTotalDistanceDataTextView.text = UtilsUI.parseNullToKilometers(route.distance)

        // Data: Location
        detailsEventLocationDataTextView.text = UtilsUI.parseNullString(route.location)
        detailsEventLocationDataTextView.isSelected = true

        // Data: Author name
        detailsEventAuthorDataTextView.text = UtilsUI.parseNullString(post.user.nickname)
        detailsEventAuthorDataTextView.isSelected = true

        // Data: Author image
        detailsEventAuthorImageView.loadCircularImage(post.user)

        // Data: Min Altitude
        detailsEventMinAltitudeDataTextView.text = UtilsUI.parseNullToMeters(route.minAltitude)

        // Data: Avg Altitude
        detailsEventAvgAltitudeDataTextView.text = UtilsUI.parseNullToMeters(route.avgAltitude)

        // Data: Max Altitude
        detailsEventMaxAltitudeDataTextView.text = UtilsUI.parseNullToMeters(route.maxAltitude)

        // Join button
        when (post.alreadyJoined) {
            true -> {
                detailsEventJoinTextView.text = getString(R.string.joined)
                detailsEventJoinRelativeLayout.background = AppCompatResources.getDrawable(
                    thisActivity,
                    R.drawable.gradient_green_light_capsule
                )
            }
            false -> {
                detailsEventJoinTextView.text = getString(R.string.join)
                detailsEventJoinRelativeLayout.background = AppCompatResources.getDrawable(
                    thisActivity,
                    R.drawable.gradient_red_capsule
                )
            }
        }
    }

    /*
    This function initializes planned workout charts.
     */
    private fun initCharts() {

        // Init data
        val elevations = post.plannedRoute?.route?.route?.map { it.elevation }!!

        // Elevation profile chart
        UtilsCharts.setUpLineChart(
            chart = detailsEventElevationProfileLineChart,
            data = elevations,
            color = Color.BLUE
        )
    }

    /*
    This function initialzies listeners on event author, join / leave button and on attendands button.
     */
    private fun initListeners() {

        // Listener: Author
        detailsEventAuthorRelativeLayout.setOnClickListener { thisActivity.openAnyUserProfile(post.user.id) }

        // Listener: Join button
        detailsEventJoinRelativeLayout.setOnClickListener {
            when (post.alreadyJoined) {
                true -> leaveEvent()
                false -> joinEvent()
            }
        }

        // Listener: Users list
        detailsEventUsersJoinedRelativeLayout.setOnClickListener {
            openDialogWindow()
        }
    }

    /*
    This function is run when user want to join actual event
     */
    private fun joinEvent() {
        ApiClient.runRequestWithoutParser(
            path = "api/events/${post.id}/enrolled/${thisActivity.currentUser.id}",
            method = "POST"
        ) {
            post.alreadyJoined = true
            activity?.runOnUiThread { initUI() }
        }
    }

    /*
    This function is run when user want to leave actual event
     */
    private fun leaveEvent() {
        ApiClient.runRequestWithoutParser(
            path = "api/events/${post.id}/enrolled/${thisActivity.currentUser.id}",
            method = "DELETE"
        ) {
            post.alreadyJoined = false
            activity?.runOnUiThread { initUI() }
        }
    }

    /*
    This function opens dialog window with joined users list.
     */
    @SuppressLint("SetTextI18n")
    private fun openDialogWindow() {

        // Init
        val dialog = Dialog(thisActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_users)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        UtilsUI.addBlur(dialog.findViewById(R.id.dialogUsersBackgroundBlurLayout), thisActivity)

        // Init UI
        val dialogTextView = dialog.findViewById(R.id.dialogUsersTextView) as TextView
        dialogTextView.text = getString(R.string.number_of_joined_users) + " 0"
        val dialogListView = dialog.findViewById(R.id.dialogUsersListView) as ListView
        val adapter = UserAdapter(thisActivity, dialog)
        dialogListView.adapter = adapter
        adapter.notifyDataSetChanged()

        // Update list of users
        updateDialogWithUsers(dialog)
        dialog.show()
    }

    /*
    This function updates list of joined users in dialog window.
     */
    @SuppressLint("SetTextI18n")
    private fun updateDialogWithUsers(dialog: Dialog) {
        ApiClient.runRequestWithParser(
            path = "api/events/${post.id}/enrolled",
            method = "GET",
            modelClass = Array<Models.FullUser>::class.java
        ) { allUsers ->
            thisActivity.runOnUiThread {

                // Init
                val dialogTextView = dialog.findViewById(R.id.dialogUsersTextView) as TextView
                val listView = dialog.findViewById(R.id.dialogUsersListView) as ListView
                val adapter = listView.adapter as UserAdapter

                // Adapter update
                adapter.dataSource.clear()
                adapter.dataSource.addAll(allUsers)
                adapter.notifyDataSetChanged()

                // Text update
                dialogTextView.text =
                    getString(R.string.number_of_joined_users) + " ${allUsers.size}"
            }
        }
    }

    /*
    This fragment should always change toolbar text to "Event details".
     */
    override fun onResume() {
        super.onResume()
        thisActivity.updateToolbar(getString(R.string.event_details))
        detailsEventMapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        detailsEventMapView?.onStart()
    }

    override fun onPause() {
        super.onPause()
        detailsEventMapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        detailsEventMapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        detailsEventMapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        detailsEventMapView?.onSaveInstanceState(outState)
    }
}