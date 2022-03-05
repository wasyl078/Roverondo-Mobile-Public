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
import com.example.roverondo_mobile.adapters.RoutesAdapter
import com.example.roverondo_mobile.api.ApiClient
import com.example.roverondo_mobile.models.Models
import com.example.roverondo_mobile.utils.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_event.*
import kotlinx.android.synthetic.main.fragment_post_route.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody


class EventFragment : Fragment(R.layout.fragment_event) {

    // Variables & consts
    private lateinit var thisActivity: GeneralActivity
    private var map: GoogleMap? = null
    private var plannedRoutePost: Models.Post? = null

    /*
    This "real constructor" for event creator initializes UI and listeners.
     */
    @SuppressLint("MissingPermission", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        thisActivity = activity as GeneralActivity
        thisActivity.invalidateOptionsMenu()
        eventMapView.onCreate(savedInstanceState)

        // Blur
        UtilsUI.addBlur(evenNewRouteBlurLayout, thisActivity)
        UtilsUI.addBlur(eventTextFieldsBlurView, thisActivity)
        UtilsUI.addBlur(eventShareEventBlurLayout, thisActivity)

        // UI Update and listeners init
        updateUI()
        initListeners()
    }

    /*
    This function initializes listeners on date and time pickers. Also There are also added listeners
    to new route button and share event button.
     */
    @SuppressLint("SetTextI18n")
    private fun initListeners() {

        // New route button listener
        evenNewRouteBlurLayout.setOnClickListener { openDialogWindow() }

        // Share event button listener
        eventShareEventRelativeLayout.setOnClickListener { shareAction() }

        // Date picker button listener
        eventDatepickerRelativeLayout.setOnClickListener {
            val datePicker = UtilsPickers.DatePickerFragment { it1, it2, it3 ->
                eventDatepickerTextView.text = "$it1-$it2-$it3"
                eventDatepickerRelativeLayout.background =
                    AppCompatResources.getDrawable(
                        thisActivity,
                        R.drawable.gradient_blue_capsule
                    )
            }
            datePicker.show(thisActivity.supportFragmentManager, Consts.DATETAG)

        }

        // Time picker button listener
        eventTimepickerRelativeLayout.setOnClickListener {
            val timePicker = UtilsPickers.TimePickerFragment { it1, it2 ->
                eventTimepickerTextView.text = "$it1:$it2"
                eventTimepickerRelativeLayout.background =
                    AppCompatResources.getDrawable(
                        thisActivity,
                        R.drawable.gradient_blue_capsule
                    )
            }
            timePicker.show(thisActivity.supportFragmentManager, Consts.TIMETAG)
        }
    }

    /*
    This function initializes share procedure. At first it checks whether text fields contains
    proper values. If yes, then a post with event object is made to API.
     */
    private fun shareAction() {
        when (checkTextFields()) {
            true -> {
                initPostEventToApi()
            }
            false -> {
            }
        }
    }

    /*
    This function check text fields. These text fields cannot be empty. And also date and time cannot
    be also as well.
     */
    private fun checkTextFields(): Boolean {

        // Init
        var status = true

        // Check route
        if (plannedRoutePost == null) {
            status = false
            evenNewRouteRelativeLayout.background =
                AppCompatResources.getDrawable(thisActivity, R.drawable.gradient_red_capsule)
        }

        // Check title
        if (eventTitleTextInputEditText.text.toString().isEmpty()) {
            status = false
            eventTitleTextInputLayout.error = "Empty title!"
            eventTitleTextInputLayout.isErrorEnabled = true
            eventTitleTextInputLayout.setErrorIconDrawable(R.drawable.ic_error)
            eventTitleTextInputLayout.boxStrokeColor = Color.RED
        } else {
            eventTitleTextInputLayout.isErrorEnabled = false
            eventTitleTextInputLayout.boxStrokeColor = thisActivity.getColor(R.color.AppLightGray)
        }

        // Description
        if (eventDescriptionTextInputEditText.text.toString().isEmpty()) {
            status = false
            eventDescriptionTextInputLayout.error = "Empty description!"
            eventDescriptionTextInputLayout.isErrorEnabled = true
            eventDescriptionTextInputLayout.setErrorIconDrawable(R.drawable.ic_error)
            eventDescriptionTextInputLayout.boxStrokeColor = Color.RED
        } else {
            eventDescriptionTextInputLayout.isErrorEnabled = false
            eventDescriptionTextInputLayout.boxStrokeColor =
                thisActivity.getColor(R.color.AppLightGray)
        }

        // Check date
        if (eventDatepickerTextView.text.toString() == getString(R.string.date_picker)) {
            status = false
            eventDatepickerRelativeLayout.background =
                AppCompatResources.getDrawable(thisActivity, R.drawable.gradient_red_capsule)
        } else {
            eventDatepickerRelativeLayout.background =
                AppCompatResources.getDrawable(thisActivity, R.drawable.gradient_blue_capsule)
        }

        // Check time
        if (eventTimepickerTextView.text.toString() == getString(R.string.time_picker)) {
            status = false
            eventTimepickerRelativeLayout.background =
                AppCompatResources.getDrawable(thisActivity, R.drawable.gradient_red_capsule)
        } else {
            eventTimepickerRelativeLayout.background =
                AppCompatResources.getDrawable(thisActivity, R.drawable.gradient_blue_capsule)
        }

        // Checkdatetime
        if (eventDatepickerTextView.text.toString() != getString(R.string.date_picker) &&
            eventTimepickerTextView.text.toString() != getString(R.string.time_picker)
        ) {
            val timestampNow = UtilsDates.milisecondsToTimestamp(System.currentTimeMillis())
            val datetimeEvent = "${eventDatepickerTextView.text} ${eventTimepickerTextView.text}"
            val timestampEvent = UtilsDates.longDateToTimestamp(datetimeEvent)
            val difference = UtilsDates.differenceBetweenTimestamps(timestampNow, timestampEvent)
            if("-" in difference){
                status = false
                UtilsAlerts.snackbar("Please choose date in future!")
                eventTimepickerRelativeLayout.background =
                    AppCompatResources.getDrawable(thisActivity, R.drawable.gradient_red_capsule)
                eventDatepickerRelativeLayout.background =
                    AppCompatResources.getDrawable(thisActivity, R.drawable.gradient_red_capsule)
            }
        }

        return status
    }

    /*
    This function updates UI. It is all about "Add route" button and map with polyline route.
     */
    @SuppressLint("MissingPermission")
    private fun updateUI() {
        when (plannedRoutePost == null) {
            true -> {
                eventMapBackgroundCardView.visibility = View.GONE
                evenNewRouteBackgroundCardView.visibility = View.VISIBLE
            }
            false -> {
                eventMapBackgroundCardView.visibility = View.VISIBLE
                evenNewRouteBackgroundCardView.visibility = View.GONE

                // Add route points to map
                val points = plannedRoutePost?.plannedRoute?.route?.route
                when (points == null) {
                    true -> UtilsAlerts.snackbar("Couldn't load route!")
                    false -> eventMapView.getMapAsync {
                        map = it
                        it.isMyLocationEnabled = true
                        MapsInitializer.initialize(thisActivity)
                        UtilsMaps.addRouteToMap(map!!, points)
                    }
                }
            }
        }
    }

    /*
    This function opens dialog window with list of routes. User should choose a route
    for event from that dialog window.
     */
    @SuppressLint("SetTextI18n")
    private fun openDialogWindow() {

        // Init
        val dialog = Dialog(thisActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_routes)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        UtilsUI.addBlur(dialog.findViewById(R.id.dialogRoutesBackgroundBlurLayout), thisActivity)

        // Init UI
        val dialogTextView = dialog.findViewById(R.id.dialogRoutesTextView) as TextView
        dialogTextView.text = getString(R.string.number_of_available_routes) + " 0"
        val dialogListView = dialog.findViewById(R.id.dialogRoutesListView) as ListView
        val adapter = RoutesAdapter(thisActivity, this,  dialog)

        // Make request
        ApiClient.runRequestWithParser(
            path = "api/wall/${thisActivity.currentUser.id}?postTypes=PlannedRoutePost&extended=true",
            method = "GET",
            modelClass = Array<Models.Post>::class.java
        ) { newItems ->

            // Update adapter
            thisActivity.runOnUiThread {
                adapter.dataSource.addAll(newItems)
                dialogListView.adapter = adapter
                adapter.notifyDataSetChanged()
                dialogTextView.text =
                    getString(R.string.number_of_available_routes) + " " + newItems.size.toString()
            }
        }

        dialog.show()
    }

    /*
    This function makes request to API to create event on server.
     */
    private fun initPostEventToApi() {

        // Init
        val bufDatetime = "${eventDatepickerTextView.text} ${eventTimepickerTextView.text}"
        val event = Models.EventToPost(
            title = eventTitleTextInputEditText.text.toString(),
            description = eventDescriptionTextInputEditText.text.toString(),
            startsAt = UtilsDates.longDateToTimestamp(bufDatetime)
        )

        // Create body
        val mediaType = "application/json".toMediaTypeOrNull()
        val bodyJson = Gson().toJson(event)
        val body = bodyJson.toRequestBody(mediaType)

        // Make request
        ApiClient.runRequestWithoutParser(
            path = "api/events/users/${thisActivity.currentUser.id}/plannedRoutes/${plannedRoutePost?.plannedRoute!!.id}",
            method = "POST",
            body = body
        ) { thisActivity.runOnUiThread { thisActivity.openWallFragment() } }
    }

    /*
    This function is used to RoutesAdapter. It sets up planned route and updates this view UI.
    When route is chosen, then "Click here to choose new route" is hidden and map with visualized
    route is brought up to front view.
     */
    fun setPlannedRoute(plannedRoutePost: Models.Post){
        this.plannedRoutePost = plannedRoutePost
        updateUI()
    }


    override fun onResume() {
        super.onResume()
        eventMapView?.onResume()
        thisActivity.updateToolbar(getString(R.string.event_creator))
    }

    override fun onStart() {
        super.onStart()
        eventMapView?.onStart()
    }

    override fun onPause() {
        super.onPause()
        eventMapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        eventMapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        eventMapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        eventMapView?.onSaveInstanceState(outState)
    }
}