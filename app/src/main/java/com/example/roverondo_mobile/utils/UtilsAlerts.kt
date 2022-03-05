package com.example.roverondo_mobile.utils

import android.app.AlertDialog
import android.content.Intent
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.roverondo_mobile.R
import com.example.roverondo_mobile.activities.GeneralActivity
import com.google.android.material.snackbar.Snackbar
import kotlin.math.max

object UtilsAlerts {
    var activity: AppCompatActivity? = null
    private var counter = 0

    /*
    This function initialzies snackbar on main view.
     */
    fun snackbar(text: String, duration: Int = Snackbar.LENGTH_SHORT) {
        // Get context view
        val contextView: View = try {
            activity?.findViewById(R.id.mainActivityRelativeLayout)!!
        } catch (e: Exception) {
            activity?.findViewById(R.id.drawer_layout)!!
        }

        // Display snackbar
        if (activity != null)
            Snackbar.make(contextView, text, duration).show()
    }

    /*
    This function initialzies simple dialog window.
     */
    fun alertDialog(text: String) {
        AlertDialog.Builder(activity)
            .setMessage(text)
            .setNegativeButton(activity?.getString(R.string.cancel), null)
            .show()
    }

    /*
    This function initialzies simple dialog window and on accept it runs given function.
     */
    fun alertDialogWithFunction(
        text: String,
        onResponseFunction: (() -> Unit)
    ) {
        AlertDialog.Builder(activity)
            .setMessage(text)
            .setPositiveButton(activity?.getString(R.string.delete)) { _, _ -> onResponseFunction.invoke() }
            .setNegativeButton(activity?.getString(R.string.cancel), null)
            .show()
    }

    /*
    This function initialzies dialog window with "Settings" button.
     */
    fun alertDialogGpsSettings(text: String) {
        AlertDialog.Builder(activity)
            .setMessage(text)
            .setPositiveButton(activity?.getString(R.string.settings)) { _, _ ->
                activity?.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton(activity?.getString(R.string.cancel), null)
            .show()
    }


    /*
    This function adds 1 to counter. That counter is used as "semaphore" for loading indicator.
    It is turned on when a call to Api is requested, and second function -> remove indicator,
    is used when call is ended and things are updated.
     */
    fun addIndicator() {
        if (activity != null && activity!!::class == GeneralActivity::class) {
            activity!!.runOnUiThread {
                counter += 1
                (activity as GeneralActivity).startLoading()
            }
        }
    }

    /*
    This function removes 1 from counter. See description in addIndicator().
     */
    fun removeIndicator() {
        if (activity != null && activity!!::class == GeneralActivity::class) {
            activity!!.runOnUiThread {
                counter = max(0, counter - 1)
                if (counter == 0)
                    stopIndicator()
            }
        }
    }

    /*
    This function waits soem time and then performs operation of total indicator stopping.
     */
    private fun stopIndicator() {
        Thread {
            var currentTime = System.currentTimeMillis()
            val stopTime = currentTime + 750

            while (currentTime < stopTime) {
                Thread.sleep(25)
                currentTime = System.currentTimeMillis()
            }
            activity!!.runOnUiThread { (activity as GeneralActivity).stopLoading() }
        }.start()
    }
}