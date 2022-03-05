package com.example.roverondo_mobile.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import com.example.roverondo_mobile.R
import com.example.roverondo_mobile.activities.GeneralActivity
import com.example.roverondo_mobile.api.ApiClient
import com.example.roverondo_mobile.models.Models
import com.example.roverondo_mobile.utils.*
import com.example.roverondo_mobile.utils.UtilsUI.loadCircularImage
import kotlinx.android.synthetic.main.fragment_other_user.*

class OtherUserFragment : Fragment(R.layout.fragment_other_user) {

    // Variables & consts
    private var currentUserId: Int = -1
    private var actualUserId: Int = -1
    private lateinit var actualFullUser: Models.FullUser
    private var actualStatistics: Models.UsersStatistics? = null
    private lateinit var currentUserFollowers: Array<Models.FullUser>
    private lateinit var thisActivity: GeneralActivity
    private var chartsData: Array<Models.ChartsPoint>? = null

    /*
    This "constructor" initializes UI, user data (and charts), and follow button.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Init variables
        thisActivity = activity as GeneralActivity
        thisActivity.invalidateOptionsMenu()

        // Init IDS
        actualUserId = arguments?.getInt(Consts.ID)!!
        currentUserId = arguments?.getInt(Consts.ID2)!!

        // Blur UI update
        UtilsUI.addBlur(otherUserBioBlurView, thisActivity)
        UtilsUI.addBlur(otherUserActivitiesBlurView, thisActivity)
        UtilsUI.addBlur(otherUserDistancesBlurView, thisActivity)
        UtilsUI.addBlur(otherUserAverageSpeedsBlurView, thisActivity)
        UtilsUI.addBlur(otherUserElevationsBlurView, thisActivity)

        // UI Update
        updateUserInfo()
    }

    /*
    This function gets all data about user from API. On success there is bio updated, and charts
    and buttons listeners.
     */
    private fun updateUserInfo() {

        // Bio
        ApiClient.runRequestWithParser(
            path = "api/users/$actualUserId",
            method = "GET",
            modelClass = Models.FullUser::class.java
        ) { fullUser ->
            actualFullUser = fullUser
            thisActivity.runOnUiThread {
                setUpBio()
                setUpListeners()
                thisActivity.updateToolbar(actualFullUser.nickname)
            }
            updateFollowButton()
        }

        // Statistics
        ApiClient.runRequestWithParser(
            path = "api/users/$actualUserId/statistics",
            method = "GET",
            modelClass = Models.UsersStatistics::class.java
        ) { stats ->
            actualStatistics = stats
            thisActivity.runOnUiThread { setUpStatistics() }
        }

        // Charts
        val getLast12MonthsDates = UtilsDates.getLast12MonthsDates()
        val intervalStart = getLast12MonthsDates.first()
        val intervalEnd = getLast12MonthsDates.last()
        ApiClient.runRequestWithParser(
            path = "api/users/$actualUserId/statisticsOverTime/$intervalStart/$intervalEnd/month",
            method = "GET",
            modelClass = Array<Models.ChartsPoint>::class.java
        ) { data ->
            chartsData = data
            thisActivity.runOnUiThread { setUpCharts() }
        }
    }

    /*
    This function updates follow button. When user is followed by current user, then that
    button is set to "Followed". When user is not yet followed then there is "Follow"
     */
    private fun updateFollowButton() {
        ApiClient.runRequestWithParser(
            path = "api/users/$currentUserId/followings",
            method = "GET",
            modelClass = Array<Models.FullUser>::class.java
        ) { followers ->
            currentUserFollowers = followers
            thisActivity.runOnUiThread {
                setUpFollowButton()
            }
        }
    }

    /*
    This function initializes users bio and some personal data.
     */
    private fun setUpBio() {
        // Top section: Image
        otherUserImageView.loadCircularImage(actualFullUser)

        // Top section: Nicname
        otherUserNameTextView.text = UtilsUI.parseNullString(actualFullUser.nickname)

        // Top section: Bio
        otherUserBioTextView.text = UtilsUI.parseNullString(actualFullUser.bio)

        // Middle section: Joined at
        otherUserJoinedAtDataTextView.text =
            UtilsDates.timestampToShortDate(actualFullUser.dateJoined)

        // Middle section: Birthdate
        otherUserBirthdateDataTextView.text = UtilsUI.parseNullDateString(actualFullUser.birthDate)

        // Middle section: Gender
        otherUserGenderDataTextView.text = UtilsUI.parseNullString(actualFullUser.gender)

        // Middle section: City
        otherUserCityDataTextView.text = UtilsUI.parseNullString(actualFullUser.city)
    }


    /*
    This function downloads and updates (on UI) users statistcs
     */
    private fun setUpStatistics() {

        // Total distance traveled
        otherUserTotalDistanceTraveledDataTextView.text =
            UtilsUI.parseNullToKilometers(actualStatistics?.totalDistanceTravelled)

        // Time in motion
        otherUserTimeInMotionDataTextView.text =
            UtilsUI.parseNullToHoursMinutes(actualStatistics?.timeInMotion)

        // Followers
        otherUserFollowersDataTextView.text = UtilsUI.parseNullString(actualStatistics?.followers)

        // Following
        otherUserFollowingDataTextView.text =
            UtilsUI.parseNullString(actualStatistics?.followings)

        // Total received reactions
        otherUserTotalReceivedReactionsDataTextView.text =
            UtilsUI.parseNullString(actualStatistics?.totalReceivedReactions)

        // Total given reactions
        otherUserTotalGivenReactionsDataTextView.text =
            UtilsUI.parseNullString(actualStatistics?.totalGivenReactions)
    }

    /*
    This function initializes user data charts.
     */
    private fun setUpCharts() {

        // Init
        val activities = mutableListOf<Int>()
        val distances = mutableListOf<Float>()
        val averageSpeeds = mutableListOf<Float>()
        val elevations = mutableListOf<Int>()
        val datesFrom = mutableListOf<String>()

        chartsData?.forEach {
            activities.add(it.activities)
            distances.add(it.distance / 1000)
            averageSpeeds.add(it.averageSpeed * 3.6F)
            elevations.add(it.elevation.toInt())
            datesFrom.add(it.from)
        }

        // Last activities
        UtilsCharts.setUpBarChart(
            chart = otherUserActivitiesBarChart,
            data = activities,
            datesFrom = datesFrom,
            color = Color.RED
        )

        // Last distances
        UtilsCharts.setUpBarChart(
            chart = otherUserDistancesBarChart,
            data = distances,
            datesFrom = datesFrom,
            color = Color.YELLOW
        )

        // Last average speeds
        UtilsCharts.setUpBarChart(
            chart = otherUserAverageSpeedsBarChart,
            data = averageSpeeds,
            datesFrom = datesFrom,
            color = Color.GREEN
        )

        // Last elevations
        UtilsCharts.setUpBarChart(
            chart = otherUserElevationsBarChart,
            data = elevations,
            datesFrom = datesFrom,
            color = Color.BLUE
        )
    }

    /*
    This function initializes listeners on followers and following buttons.
     */
    private fun setUpListeners() {

        // Followers
        otherUserFollowersTextView.setOnClickListener {
            thisActivity.openFollowersFollowing(actualFullUser.id, Consts.FOLLOWERS)
        }

        otherUserFollowersDataTextView.setOnClickListener {
            thisActivity.openFollowersFollowing(actualFullUser.id, Consts.FOLLOWERS)
        }

        // Following
        otherUserFollowingTextView.setOnClickListener {
            thisActivity.openFollowersFollowing(actualFullUser.id, Consts.FOLLOWING)
        }

        otherUserFollowingDataTextView.setOnClickListener {
            thisActivity.openFollowersFollowing(actualFullUser.id, Consts.FOLLOWING)
        }
    }

    /*
    This function initializes follow button. Color, icon and text is changed according
    to followed / not yet followed state.
     */
    private fun setUpFollowButton() {

        // Init state
        var alreadyFollowed = false
        currentUserFollowers.forEach {
            if (it.id == actualFullUser.id)
                alreadyFollowed = true
        }

        // Init colors and texts
        val thumbUpDrawable = AppCompatResources.getDrawable(thisActivity, R.drawable.ic_check)
        val plusDrawable = AppCompatResources.getDrawable(thisActivity, R.drawable.ic_add)
        val blueBackground =
            AppCompatResources.getDrawable(thisActivity, R.drawable.gradient_green_light_capsule)
        val redBackground =
            AppCompatResources.getDrawable(thisActivity, R.drawable.gradient_red_capsule)

        // Set up colors and texts
        when (alreadyFollowed) {
            true -> {
                otherUserFollowRelativeLayout.background = blueBackground
                otherUserFollowImageView.setImageDrawable(thumbUpDrawable)
                otherUserFollowTextView.text = thisActivity.getString(R.string.followed)
            }
            false -> {
                otherUserFollowRelativeLayout.background = redBackground
                otherUserFollowImageView.setImageDrawable(plusDrawable)
                otherUserFollowTextView.text = thisActivity.getString(R.string.follow)
            }
        }

        // Add listeners
        otherUserFollowRelativeLayout.setOnClickListener {

            // Init
            val followedId = actualFullUser.id
            val followerId = currentUserId
            val path = "api/users/$followedId/$followerId"
            val method = if (alreadyFollowed) "DELETE" else "POST"

            // Make request
            ApiClient.runRequestWithoutParser(
                path = path,
                method = method
            ) {
                thisActivity.runOnUiThread {
                    updateFollowButton()
                }
            }
        }
    }


    /*
    This fragment should always change toolbar text to users nickname or loading...
     */
    override fun onResume() {
        super.onResume()
        if (::actualFullUser.isInitialized)
            thisActivity.updateToolbar(actualFullUser.nickname)
        else
            thisActivity.updateToolbar(getString(R.string.placeholder))
    }
}