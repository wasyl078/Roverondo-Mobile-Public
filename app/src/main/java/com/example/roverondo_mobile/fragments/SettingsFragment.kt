package com.example.roverondo_mobile.fragments

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.GridView
import androidx.fragment.app.Fragment
import com.example.roverondo_mobile.R
import com.example.roverondo_mobile.activities.GeneralActivity
import com.example.roverondo_mobile.services.TrackingService
import com.example.roverondo_mobile.utils.Consts
import com.example.roverondo_mobile.utils.UtilsMaps
import com.example.roverondo_mobile.utils.UtilsAlerts
import com.example.roverondo_mobile.utils.UtilsUI
import com.google.android.gms.location.LocationRequest
import kotlinx.android.synthetic.main.fragment_settings.*
import java.util.*

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    // Variables & consts
    private lateinit var thisActivity: GeneralActivity

    /*
    This "constructor" additionally invalidates options menu and set ups UI and lsiteners.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        thisActivity = activity as GeneralActivity
        thisActivity.invalidateOptionsMenu()

        // Init blurs
        UtilsUI.addBlur(settingsColorBackgroundBlurLayout, thisActivity)
        UtilsUI.addBlur(settingsFollowColorBackgroundBlurLayout, thisActivity)
        UtilsUI.addBlur(settingsBatteryBackgroundBlurLayout, thisActivity)

        // Init UI
        settingsColorImageView.setBackgroundColor(UtilsMaps.ROUTE_COLOR)
        settingsFollowColorImageView.setBackgroundColor(UtilsMaps.FOLLOW_ROUTE_COLOR)

        // Init listeners
        settingsColorRelativeLayout.setOnClickListener { openColorsDialogWindow(Consts.GENERAL_ROUTE_TYPE) }
        settingsFollowColorRelativeLayout.setOnClickListener { openColorsDialogWindow(Consts.FOLLOW_ROUTE_TYPE) }
        settingsBatterySwitc.setOnCheckedChangeListener { _, isChecked ->
            updateBatteryMode(isChecked)
        }
    }

    /*
    This function opens dialog window woth grid of colors to choose.
     */
    private fun openColorsDialogWindow(routeType: String) {

        // Create grid view
        val gridView = GridView(thisActivity)
        val rawColors = resources.getStringArray(R.array.available_trips_colors)
        val availableColors = ArrayList<Int>()
        val bufforList = rawColors.map {
            availableColors.add(Color.parseColor(it))
            ""
        }

        //Create dialog window
        val dialog = Dialog(thisActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(gridView)

        // Create adapter - set colors for each cell
        val adapter: ArrayAdapter<String> = object :
            ArrayAdapter<String>(thisActivity, android.R.layout.simple_list_item_1, bufforList) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val color = availableColors[position]
                view.setBackgroundColor(color)
                return view
            }
        }
        gridView.adapter = adapter
        gridView.numColumns = 5

        // Add listener
        gridView.onItemClickListener =
            AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->

                // Change color
                val newColor = availableColors[position]
                when(routeType){
                    Consts.GENERAL_ROUTE_TYPE -> {
                        UtilsMaps.ROUTE_COLOR = newColor
                        settingsColorImageView.setBackgroundColor(newColor)
                    }
                    Consts.FOLLOW_ROUTE_TYPE -> {
                        UtilsMaps.FOLLOW_ROUTE_COLOR = newColor
                        settingsFollowColorImageView.setBackgroundColor(newColor)
                    }
                }

                // Make snackbar and close dialog window
                UtilsAlerts.snackbar("Color updated")
                dialog.dismiss()
            }

        // Set dialog window wider
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(Objects.requireNonNull(dialog.window)?.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        dialog.window!!.attributes = lp
        dialog.show()
    }

    /*
    This function changes battery saving mode connexted to receiving GPS location.
     */
    private fun updateBatteryMode(isChecked: Boolean) {
        when (isChecked) {
            true -> {
                UtilsAlerts.snackbar("Battery saving mode ON")
                TrackingService.LOCATION_PRIORITY = LocationRequest.PRIORITY_LOW_POWER
            }
            false -> {
                UtilsAlerts.snackbar("Battery saving mode OFF")
                TrackingService.LOCATION_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
        }
    }

    /*
    This fragment should always change toolbar text to "Settings".
     */
    override fun onResume() {
        super.onResume()
        thisActivity.updateToolbar(getString(R.string.settings))
        thisActivity.updateNavigationCheckedItem(R.id.nav_settings)
    }
}