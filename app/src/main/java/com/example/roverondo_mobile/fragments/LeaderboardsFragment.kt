package com.example.roverondo_mobile.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.roverondo_mobile.R
import com.example.roverondo_mobile.activities.GeneralActivity
import com.example.roverondo_mobile.adapters.UserAwardAdapter
import com.example.roverondo_mobile.api.ApiClient
import com.example.roverondo_mobile.models.Models
import com.example.roverondo_mobile.utils.UtilsAlerts
import com.example.roverondo_mobile.utils.UtilsUI
import kotlinx.android.synthetic.main.fragment_leaderboards.*

class LeaderboardsFragment : Fragment(R.layout.fragment_leaderboards) {

    private lateinit var thisActivity: GeneralActivity
    private lateinit var adapter: UserAwardAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        thisActivity = activity as GeneralActivity
        thisActivity.invalidateOptionsMenu()

        // Blur
        UtilsUI.addBlur(leaderboardsBlurLayout, thisActivity)

        //leaderboardsListView.adapter = UserAwardAdapter()
        initAdapter()
    }

    /*
   This function initializes adapter for user list view.
    */
    private fun initAdapter() {

        // Init
        adapter = UserAwardAdapter(thisActivity)
        leaderboardsListView.adapter = adapter

        // Make request to API
        ApiClient.runRequestWithParser(
            path = "api/users",
            method = "GET",
            modelClass = Array<Models.FullUser>::class.java
        ) {
            thisActivity.runOnUiThread {
                adapter.dataSource.addAll(it)
                adapter.notifyDataSetChanged()
                collectStatistics()
            }
        }
    }

    /*
    This function collects statistics for each user in list, and after getting all statistics, the list
    of users in leaderboards is updated.
     */
    private fun collectStatistics() {

        // Init
        var globalCounter = 0
        val responsesHashMap = hashMapOf<Int, Models.UsersStatistics?>()

        // Fill with nulls
        adapter.dataSource.forEach { user -> responsesHashMap[user.id] = null }

        // Get statistics
        adapter.dataSource.forEach { user ->

            ApiClient.runRequestWithParser(
                path = "api/users/${user.id}/statistics",
                method = "GET",
                modelClass = Models.UsersStatistics::class.java
            ) { stats ->
                thisActivity.runOnUiThread {
                    responsesHashMap[user.id] = stats
                    globalCounter += 1
                }
            }
        }


        // Run 10 seconds while loop to get all statistics
        Thread {
            var counter = 0
            while (counter < 10) {

                // Wait
                Thread.sleep(1000)

                // Check whether all responses are finished
                if (globalCounter == responsesHashMap.size) {

                    // Update list
                    adapter.dataSource.forEach {
                        it.statistics = responsesHashMap[it.id]
                        val bufDistanceTravelled = responsesHashMap[it.id]!!.totalDistanceTravelled
                        val kilometers = UtilsUI.parseNullToKilometers(bufDistanceTravelled)
                        it.nickname = "[${kilometers}] ${it.nickname}"
                    }
                    adapter.dataSource.sortByDescending { it.statistics!!.totalDistanceTravelled }
                    thisActivity.runOnUiThread { adapter.notifyDataSetChanged() }

                    // Break loop and thread, stop loading animation
                    UtilsAlerts.removeIndicator()
                    return@Thread
                }
                counter += 1
            }
            UtilsAlerts.snackbar("Failure! Could not connect to statistics API!")
            UtilsAlerts.removeIndicator()
        }.start()
    }

    /*
    This fragment should always change toolbar text to "Leaderboards"
     */
    override fun onResume() {
        super.onResume()
        thisActivity.updateToolbar(getString(R.string.leaderboards))
        thisActivity.updateNavigationCheckedItem(R.id.nav_leaderboards)
    }
}