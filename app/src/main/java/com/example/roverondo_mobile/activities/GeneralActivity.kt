package com.example.roverondo_mobile.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.iterator
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.roverondo_mobile.R
import com.example.roverondo_mobile.fragments.*
import com.example.roverondo_mobile.models.Models
import com.example.roverondo_mobile.services.TrackedRoute
import com.example.roverondo_mobile.utils.Consts
import com.example.roverondo_mobile.utils.UtilsAlerts
import com.example.roverondo_mobile.utils.UtilsUI
import com.example.roverondo_mobile.utils.UtilsUI.loadCircularImage
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.material.navigation.NavigationView
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_general.*
import kotlinx.android.synthetic.main.navigation_header.*
import kotlin.system.exitProcess


class GeneralActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    // Variables
    lateinit var currentUser: Models.FullUser

    /*
    This "constructor" for general activity set-ups Calligraphy, transparent status bar and
    navigation bar. Also set-ups Navigation drawer - left side panel. Here is called setting
    Google Maps options too.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Snackbar init
        UtilsAlerts.activity = this

        // Init user
        currentUser = intent.getSerializableExtra(Consts.USER) as Models.FullUser
        
        // Init UI
        setContentView(R.layout.activity_general)
        UtilsUI.setTransparentTopBottomBards(this)
        updateNavHeader()
        setNavigationDrawer(savedInstanceState)

        // Init Maps Options
        setUpMapsOption()
    }

    /*
    This function updates top navigation header in navigation drawer + adds listeners to users data.
     */
    @SuppressLint("SetTextI18n")
    private fun updateNavHeader() {

        // Layout style
        Thread {

            // Waiting
            Thread.sleep(250)

            // Get required height
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            val result = resources.getDimensionPixelSize(resourceId)

            runOnUiThread {

                // Update params
                nav_heder_relativeLayout.layoutParams = LinearLayout.LayoutParams(
                    nav_heder_relativeLayout.layoutParams.width,
                    nav_heder_relativeLayout.layoutParams.height + result
                )

                // Update user data
                headerUserTextView.text = currentUser.nickname
                headerUserTextView.isSelected = true
                headerUserImageView.loadCircularImage(currentUser, borderColor = Color.WHITE)

                // Listeners
                headerUserImageView.setOnClickListener { openAnyUserProfile(currentUser.id) }
                headerUserTextView.setOnClickListener { openAnyUserProfile(currentUser.id) }
            }

        }.start()
    }

    /*
    This function initializes navigation drawer and first fragment - Wall Fragment.
     */
    private fun setNavigationDrawer(savedInstanceState: Bundle?) {

        // Init
        setSupportActionBar(generalToolbar)

        // Listener
        generalNavView.setNavigationItemSelectedListener(this)
        val toggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            generalToolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        // Color of status bar
        drawer_layout.setStatusBarBackground(
            ContextCompat.getDrawable(
                this,
                R.drawable.gradient_green_rectangle
            )
        )

        // Initialize first fragment
        if (savedInstanceState == null)
            replaceFragment(WallFragment(), R.id.nav_wall)
    }

    /*
    Here are initialized Google Maps options. These options are e.g.: lite mode, map type.
     */
    private fun setUpMapsOption() {
        GoogleMapOptions()
            .liteMode(true)
            .mapType(GoogleMap.MAP_TYPE_NORMAL)
            .ambientEnabled(true)
    }

    /*
    This is on navigation drawer item click listener. It opens new fragments or initializes
    logout procedure.
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val fragment = when (item.itemId) {
            R.id.nav_wall -> WallFragment()
            R.id.nav_new_route -> TrackingFragment()
            R.id.nav_set_up_route -> SetUpRouteFragment()
            R.id.nav_event -> EventFragment()
            R.id.nav_leaderboards -> LeaderboardsFragment()
            R.id.nav_settings -> SettingsFragment()
            R.id.nav_search_users -> SearchUsersFragment()
            R.id.nav_logout -> return logout()
            else -> throw NoClassDefFoundError()
        }

        replaceFragment(fragment, item.itemId)
        return true
    }

    /*
    This functions takes care of showing / hidding three dots in right top corner in toolbar.
    That button may be visible only in Wall Fragment.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_items, menu)
        return when (supportFragmentManager.findFragmentByTag(Consts.FRAGMENT)) {
            is WallFragment -> true
            null -> true
            else -> false
        }
    }

    /*
    This functions listen for clicks on expandable toolbar items. This toolbar is used to filter
    posts on users Wall.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // Init
        val currentFragment = supportFragmentManager.findFragmentByTag(Consts.FRAGMENT)

        // When current fragment is Wall fragment
        if (currentFragment is WallFragment) {
            val filter = when (item.itemId) {
                R.id.toolbarAllItem -> Consts.FILTER_ALL
                R.id.toolbarWorkoutsItem -> Consts.FILTER_WORKOUTS
                R.id.toolbarEventsItem -> Consts.FILTER_EVENTS
                R.id.toolbarRoutesItem -> Consts.FILTER_ROUTES
                else -> throw NoSuchFieldError()
            }
            currentFragment.useFilter(filter)
        }
        return super.onOptionsItemSelected(item)
    }

    /*
    Opening any Other User Profile.
     */
    fun openAnyUserProfile(id: Int) {
        val fragment = when (id) {
            currentUser.id -> UserProfileFragment()
            else -> OtherUserFragment()
        }

        val bundle = Bundle()
        bundle.putInt(Consts.ID, id)
        bundle.putInt(Consts.ID2, currentUser.id)
        fragment.arguments = bundle
        replaceFragment(fragment)
    }

    /*
    Opening Followers / Following Users Fragment.
     */
    fun openFollowersFollowing(id: Int, follow: String) {
        val title: String
        val param: String
        if (follow == Consts.FOLLOWING) {
            title = "Following"
            param = "followings"
        } else {
            title = "Followers"
            param = "followers"
        }

        val fragment = FollowersFollowingFragment()
        val bundle = Bundle()
        bundle.putInt(Consts.ID, id)
        bundle.putString(Consts.TITLE, title)
        bundle.putString(Consts.FOLLOW, param)
        fragment.arguments = bundle
        replaceFragment(fragment)
    }

    /*
    Opening Any Deatils Fragment: Workout Details, Rute Details or Event details.
     */
    fun openDetailsFragment(postType: String, post: Models.Post) {
        val fragment: Fragment = when (postType) {
            Consts.TYPE_WORKOUT -> DetailsWorkoutFragment()
            Consts.TYPE_ROUTE -> DetailsRouteFragment()
            Consts.TYPE_EVENT -> DetailsEventFragment()
            else -> throw NoSuchFieldError()
        }
        val bundle = Bundle()
        bundle.putSerializable(Consts.POST, post)
        fragment.arguments = bundle
        replaceFragment(fragment)
    }

    /*
    Opening Post Workout Fragment with passing Route object.
     */
    fun openPostWorkout(trackedRoute: TrackedRoute) {
        val fragment = PostWorkoutFragment()
        val bundle = Bundle()
        bundle.putSerializable(Consts.ROUTE, trackedRoute)
        fragment.arguments = bundle
        replaceFragment(fragment)
    }

    /*
    Opening Wall Fragment
     */
    fun openWallFragment() {
        val fragment = WallFragment()
        replaceFragment(fragment)
        updateNavigationCheckedItem(R.id.nav_wall)
    }

    /*
    Opening Post Route Fragment with passing points and distance.
     */
    fun openPostRouteFragment(points: ArrayList<Models.Point>, distance: Double) {
        val fragment = PostRouteFragment()
        val bundle = Bundle()
        bundle.putSerializable(Consts.POINTS, points)
        bundle.putSerializable(Consts.DISTANCE, distance)
        fragment.arguments = bundle
        replaceFragment(fragment)
        updateNavigationCheckedItem(R.id.nav_set_up_route)
    }

    /*
    Opening Tracking Fragment with planned route.
     */
    fun openTrackingFragment(postId: Int) {
        val fragment = TrackingFragment()
        val bundle = Bundle()
        bundle.putInt(Consts.ID, postId)
        fragment.arguments = bundle
        replaceFragment(fragment)
        updateNavigationCheckedItem(R.id.nav_new_route)
        updateToolbar(getString(R.string.following_route))
    }

    /*
    This function hides soft keyboard from screen.
     */
    fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        var view = currentFocus
        if (currentFocus == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    /*
    Changing toolbar title.
     */
    fun updateToolbar(title: String) {
        generalToolbar.title = title
    }

    /*
    This function makes loading indicator visible.
     */
    @SuppressLint("SwitchIntDef")
    fun startLoading() {
        generalLinearProgressIndicator.show()
    }

    /*
    This function makes loading indicator not visible, gone.
     */
    @SuppressLint("SwitchIntDef")
    fun stopLoading() {
        generalLinearProgressIndicator.hide()
    }

    /*
    Changing navigation drawer checked item.
     */
    fun updateNavigationCheckedItem(id: Int) {
        val index = when (id) {
            R.id.nav_wall -> 0
            R.id.nav_new_route -> 1
            R.id.nav_set_up_route -> 2
            R.id.nav_event -> 3
            R.id.nav_leaderboards -> 4
            R.id.nav_search_users -> 5
            R.id.nav_settings -> 6
            else -> return
        }
        generalNavView.menu.getItem(index).isChecked = true
    }

    /*
    Replacing fragment with new one and updating navigation drawer checked items.
     */
    private fun replaceFragment(newFragment: Fragment, checkedItem: Int? = null) {

        // Replace fragment
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragmentContainer,
                newFragment,
                Consts.FRAGMENT
            )
            .addToBackStack(null)
            .commit()

        // Close drawer
        drawer_layout.closeDrawer(GravityCompat.START)

        // Change selected item in drawer
        when (checkedItem) {
            null -> generalNavView.menu.iterator().forEach { it.isChecked = false }
            else -> generalNavView.setCheckedItem(checkedItem)
        }

        // Hide keyboard
        hideKeyboard()
    }

    /*
    Performing logout operation from application.
     */
    private fun logout(): Boolean {
        val loginActivity = Intent(this, LoginActivity::class.java)
        val bundle = Bundle()
        bundle.putSerializable(Consts.LOGOUT_CODE, Consts.LOGOUT_CODE)
        loginActivity.putExtras(bundle)
        startActivity(loginActivity)
        finish()
        return true
    }

    /*
    This function needs to be overriden for Calligraphy to properly work.
    It changes all fonts in this activity to Almarai Light
     */
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    /*
    Disabling slidoing out navigation drawer.
     */
    fun disableNavigationDrawer() {
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    /*
    Enabling slidoing out navigation drawer.
     */
    fun enableNavigationDrawer() {
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    /*
    This overriden function prevents user from going to Login Screen when navigation drawer
    is expanded.
     */
    override fun onBackPressed() {

        when (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            true -> drawer_layout.closeDrawer(GravityCompat.START)
            false -> when (val currentFragment =
                supportFragmentManager.findFragmentByTag(Consts.FRAGMENT)) {
                is TrackingFragment -> {
                    when (currentFragment.isTracking) {
                        true -> UtilsAlerts.alertDialog(getString(R.string.you_cannot_go_back_now))
                        false -> super.onBackPressed()
                    }
                }
                is WallFragment -> { // Cannot go back from that view }
                }
                else -> super.onBackPressed()
            }
        }
    }

    /*
    This overriden function makes user loose all tracking progress when app is killed. It is
    desired effect.
     */
    override fun onDestroy() {
        super.onDestroy()
        exitProcess(0)
    }
}