package com.example.roverondo_mobile.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.Window
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.roverondo_mobile.R
import com.example.roverondo_mobile.activities.GeneralActivity
import com.example.roverondo_mobile.api.ApiClient
import com.example.roverondo_mobile.models.Models
import com.example.roverondo_mobile.utils.*
import com.example.roverondo_mobile.utils.UtilsUI.loadCircularImage
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import eightbitlab.com.blurview.BlurView
import kotlinx.android.synthetic.main.fragment_user_profile.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {

    // Variables & consts
    private var currentUserId: Int = -1
    private lateinit var currentFullUser: Models.FullUser
    private var currentStatistics: Models.UsersStatistics? = null
    private var chartsData: Array<Models.ChartsPoint>? = null
    private lateinit var thisActivity: GeneralActivity

    /*
    This "constructor" initializes UI and user data (and charts).
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Init variables
        thisActivity = activity as GeneralActivity
        thisActivity.invalidateOptionsMenu()
        currentUserId = arguments?.getInt(Consts.ID)!!

        // Blur UI update
        UtilsUI.addBlur(currentUserBioBlurView, thisActivity)
        UtilsUI.addBlur(currentUserActivitiesBlurView, thisActivity)
        UtilsUI.addBlur(currentUserDistancesBlurView, thisActivity)
        UtilsUI.addBlur(currentUserAverageSpeedsBlurView, thisActivity)
        UtilsUI.addBlur(currentUserElevationsBlurView, thisActivity)

        // UI Update
        updateWholeUI()
    }

    private fun updateWholeUI() {

        // Bio
        ApiClient.runRequestWithParser(
            path = "api/users/$currentUserId",
            method = "GET",
            modelClass = Models.FullUser::class.java
        ) { fullUser ->
            currentFullUser = fullUser
            thisActivity.runOnUiThread {
                setUpBio()
                setUpListeners()
            }
        }

        // Statistics
        ApiClient.runRequestWithParser(
            path = "api/users/$currentUserId/statistics",
            method = "GET",
            modelClass = Models.UsersStatistics::class.java
        ) { stats ->
            currentStatistics = stats
            thisActivity.runOnUiThread { setUpStatistics() }
        }

        // Charts
        val getLast12MonthsDates = UtilsDates.getLast12MonthsDates()
        val intervalStart = getLast12MonthsDates.first()
        val intervalEnd = getLast12MonthsDates.last()
        ApiClient.runRequestWithParser(
            path = "api/users/$currentUserId/statisticsOverTime/$intervalStart/$intervalEnd/month",
            method = "GET",
            modelClass = Array<Models.ChartsPoint>::class.java
        ) { data ->
            chartsData = data
            if (chartsData!!.isNotEmpty())
                thisActivity.runOnUiThread { setUpCharts() }
        }
    }

    /*
    This function initializes users bio and some personal data.
     */
    private fun setUpBio() {

        // Top section: Image
        currentUserImageView.loadCircularImage(currentFullUser)

        // Top section: Nicname
        currentUserNameTextView.text = UtilsUI.parseNullString(currentFullUser.nickname)

        // Top section: Bio
        currentUserBioTextView.text = UtilsUI.parseNullString(currentFullUser.bio)

        // Middle section: Joined at
        currentUserJoinedAtDataTextView.text =
            UtilsDates.timestampToShortDate(currentFullUser.dateJoined)

        // Middle section: Gender
        currentUserGenderDataTextView.text = UtilsUI.parseNullString(currentFullUser.gender)

        // Middle section: Birthdate
        currentUserBirthdateDataTextView.text =
            UtilsUI.parseNullDateString(currentFullUser.birthDate)

        // Middle section: Weight
        currentUserWeightDataTextView.text =
            UtilsUI.parseNullString(currentFullUser.weight, extraString = "kg")

        // Middle section: City
        currentUserCityDataTextView.text = UtilsUI.parseNullString(currentFullUser.city)
    }

    /*
    This function downloads and updates (on UI) users statistcs
     */
    private fun setUpStatistics() {

        // Total distance traveled
        currentUserTotalDistanceTraveledDataTextView.text =
            UtilsUI.parseNullToKilometers(currentStatistics?.totalDistanceTravelled)

        // Time in motion
        currentUserTimeInMotionDataTextView.text =
            UtilsUI.parseNullToHoursMinutes(currentStatistics?.timeInMotion)

        // Followers
        currentUserFollowersDataTextView.text =
            UtilsUI.parseNullString(currentStatistics?.followers)

        // Following
        currentUserFollowingDataTextView.text =
            UtilsUI.parseNullString(currentStatistics?.followings)

        // Total received reactions
        currentUserTotalReceivedReactionsDataTextView.text =
            UtilsUI.parseNullString(currentStatistics?.totalReceivedReactions)

        // Total given reactions
        currentUserTotalGivenReactionsDataTextView.text =
            UtilsUI.parseNullString(currentStatistics?.totalGivenReactions)
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
            chart = currentUserActivitiesBarChart,
            data = activities,
            datesFrom = datesFrom,
            color = Color.RED
        )

        // Last distances
        UtilsCharts.setUpBarChart(
            chart = currentUserDistancesBarChart,
            data = distances,
            datesFrom = datesFrom,
            color = Color.YELLOW
        )

        // Last average speeds
        UtilsCharts.setUpBarChart(
            chart = currentUserAverageSpeedsBarChart,
            data = averageSpeeds,
            datesFrom = datesFrom,
            color = Color.GREEN
        )

        // Last elevations
        UtilsCharts.setUpBarChart(
            chart = currentUserElevationsBarChart,
            data = elevations,
            datesFrom = datesFrom,
            color = Color.BLUE
        )
    }

    /*
    This function initializes listeners on bio, weight, city, gender, followers and following buttons.
     */
    private fun setUpListeners() {

        // Editing bio
        currentUserPencilEditView.setOnClickListener { openEditValueDialogWindow(Consts.EDIT_BIO) }

        // Editing gender
        currentUserGenderDataTextView.setOnClickListener { openGenderDialogWindow() }
        currentUserGenderTextView.setOnClickListener { openGenderDialogWindow() }

        // Editing weight
        currentUserWeightDataTextView.setOnClickListener { openEditValueDialogWindow(Consts.EDIT_WEIGHT) }
        currentUserWeightTextView.setOnClickListener { openEditValueDialogWindow(Consts.EDIT_WEIGHT) }

        // Editing city
        currentUserCityDataTextView.setOnClickListener { openEditValueDialogWindow(Consts.EDIT_CITY) }
        currentUserCityTextView.setOnClickListener { openEditValueDialogWindow(Consts.EDIT_CITY) }

        // Followers
        currentUserFollowersTextView.setOnClickListener {
            thisActivity.openFollowersFollowing(currentFullUser.id, Consts.FOLLOWERS)
        }

        currentUserFollowersDataTextView.setOnClickListener {
            thisActivity.openFollowersFollowing(currentFullUser.id, Consts.FOLLOWERS)
        }

        // Following
        currentUserFollowingTextView.setOnClickListener {
            thisActivity.openFollowersFollowing(currentFullUser.id, Consts.FOLLOWING)
        }

        currentUserFollowingDataTextView.setOnClickListener {
            thisActivity.openFollowersFollowing(currentFullUser.id, Consts.FOLLOWING)
        }
    }

    /*
    This function opens modal dialog window for editing given field.
     */
    @SuppressLint("SetTextI18n")
    private fun openEditValueDialogWindow(editValue: String) {

        // Init dialog
        val dialog = Dialog(thisActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_user_profile_editing)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Blur
        val blur =
            dialog.findViewById(R.id.dialogUserProfileEditingBackgroundBlurLayout) as BlurView
        UtilsUI.addBlur(blur, thisActivity)

        // Top text
        val topTextTextView = dialog.findViewById(R.id.dialogUserProfileEditingTextView) as TextView

        // Edit text fields
        val editText =
            dialog.findViewById(R.id.dialogUserProfileEditingTextInputEditText) as TextInputEditText
        val editLayout =
            dialog.findViewById(R.id.dialogUserProfileEditingTextInputLayout) as TextInputLayout

        when (editValue) {
            Consts.EDIT_BIO -> {
                topTextTextView.text = getString(R.string.edit_your_profile) + " bio:"
                editLayout.hint = "Bio"
                editText.setText(currentFullUser.bio)
                editText.inputType = InputType.TYPE_CLASS_TEXT
            }
            Consts.EDIT_WEIGHT -> {
                topTextTextView.text = getString(R.string.edit_your_profile) + " weight:"
                editLayout.hint = "Weight"
                editText.setText(currentFullUser.weight)
                editText.inputType = InputType.TYPE_CLASS_NUMBER
            }
            Consts.EDIT_CITY -> {
                topTextTextView.text = getString(R.string.edit_your_profile) + " city:"
                editLayout.hint = "City"
                editText.setText(currentFullUser.city)
                editText.inputType = InputType.TYPE_CLASS_TEXT
            }
            else -> return
        }

        // Save button
        val saveButton =
            dialog.findViewById(R.id.dialogUserProfileEditingSaveRelativeLayout) as RelativeLayout
        saveButton.setOnClickListener {

            // Init value
            var newText: String? = editText.text.toString()
            if (newText == "")
                newText = null

            // Update data and texts
            when (editValue) {
                Consts.EDIT_BIO -> currentFullUser.bio = newText
                Consts.EDIT_WEIGHT -> currentFullUser.weight = newText
                Consts.EDIT_CITY -> currentFullUser.city = newText
            }
            updateUserWithApi()
            dialog.dismiss()
        }

        // Cancel button
        val cancelButton =
            dialog.findViewById(R.id.dialogUserProfileEditingCancelRelativeLayout) as RelativeLayout
        cancelButton.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    /*
    This function opens modal dialog window for editing gender field.
     */
    private fun openGenderDialogWindow() {

        // Init dialog
        val dialog = Dialog(thisActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_user_profile_gender_editing)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Blur
        val blur =
            dialog.findViewById(R.id.dialogUserProfileGenderEditingBackgroundBlurLayout) as BlurView
        UtilsUI.addBlur(blur, thisActivity)

        // Female button
        val femaleButton =
            dialog.findViewById(R.id.dialogUserProfileGenderEditingFemaleRelativeLayout) as RelativeLayout
        femaleButton.setOnClickListener {
            currentFullUser.gender = Consts.FEMALE
            updateUserWithApi()
            dialog.dismiss()
        }

        // Male button
        val maleButton =
            dialog.findViewById(R.id.dialogUserProfileGenderEditingMaleRelativeLayout) as RelativeLayout
        maleButton.setOnClickListener {
            currentFullUser.gender = Consts.MALE
            updateUserWithApi()
            dialog.dismiss()
        }

        // Not specified button
        val notSpecifiedButton =
            dialog.findViewById(R.id.dialogUserProfileGenderEditingNotSpecifiedRelativeLayout) as RelativeLayout
        notSpecifiedButton.setOnClickListener {
            currentFullUser.gender = Consts.NOT_SPECIFIED
            updateUserWithApi()
            dialog.dismiss()
        }

        // Cancel button
        val cancelButton =
            dialog.findViewById(R.id.dialogUserProfileGenderEditingCancelRelativeLayout) as RelativeLayout
        cancelButton.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    /*
    This function creates and makes request to API that is supposed to update user profile data.
     */
    private fun updateUserWithApi() {

        // Create request body
        val gson = Gson()
        val mediaType = "application/json".toMediaTypeOrNull()
        val bodyJson = gson.toJson(currentFullUser)
        val body = bodyJson.toRequestBody(mediaType)

        // Make request
        ApiClient.runRequestWithoutParser(
            path = "api/users/${currentFullUser.id}",
            method = "PUT",
            body = body,
            onWrongCodeFunction = { thisActivity.runOnUiThread { updateWholeUI() } },
            onResponseFunction = { thisActivity.runOnUiThread { updateWholeUI() } }
        )
    }

    /*
    This fragment should always change toolbar text to "Your profile".
     */
    override fun onResume() {
        super.onResume()
        thisActivity.updateToolbar(getString(R.string.your_profile))
    }
}