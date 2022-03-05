package com.example.roverondo_mobile.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.auth0.android.result.UserProfile
import com.example.roverondo_mobile.R
import com.example.roverondo_mobile.api.ApiClient
import com.example.roverondo_mobile.models.Models
import com.example.roverondo_mobile.utils.Consts
import com.example.roverondo_mobile.utils.UtilsAlerts
import com.example.roverondo_mobile.utils.UtilsUI
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_login.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.util.*
import kotlin.system.exitProcess


class LoginActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    // Variables & consts
    private lateinit var account: Auth0
    private var cachedCredentials: Credentials? = null
    private var cachedUserProfile: UserProfile? = null

    /*
    This "constructor" for login activity set-ups Calligraphy, transparent status bar and
    navigation bar. Also it catches logout action from GeneralActivity. It asks user for
    location permissions.
     */
    override fun onCreate(savedInstanceState: Bundle?) {

        // Init general UI
        super.onCreate(savedInstanceState)
        UtilsUI.setCalligraphy()
        UtilsUI.setTransparentTopBottomBards(this)
        setContentView(R.layout.activity_login)

        // Snackbar init
        UtilsAlerts.activity = this

        // Init authorization UI and init UI
        account =
            Auth0(getString(R.string.com_auth0_client_id), getString(R.string.com_auth0_domain))
        updateUI()

        // Catch logout action from GeneralActivity
        if (intent.getSerializableExtra(Consts.LOGOUT_CODE) != null)
            logout()

        // Set random image in background
        putRandomImage()

        // Listeners
        loginButtonBlurLayout.setOnClickListener { loginWithBrowser() }

        // Blurs
        UtilsUI.addBlur(LoginHelloBlurLayout, this)
        UtilsUI.addBlur(loginButtonBlurLayout, this)

        // GPS permissions and battery optimization
        checkGpsPermissions()
        batteryOptimization()
    }

    /*
    This function randomly changes image in background on login screen.
     */
    private fun putRandomImage() {
        val images = listOf(
            R.drawable.bicycle_image_1,
            R.drawable.bicycle_image_2,
            R.drawable.bicycle_image_3,
            R.drawable.bicycle_image_4,
            R.drawable.bicycle_image_5,
            R.drawable.bicycle_image_6,
            R.drawable.bicycle_image_7,
            R.drawable.bicycle_image_8,
            R.drawable.bicycle_image_9,
            R.drawable.bicycle_image_10,
            R.drawable.bicycle_image_11
        )
        loginBicycleImageView.setImageDrawable(
            AppCompatResources.getDrawable(
                this,
                images.random()
            )
        )
    }

    /*
    This function updates UI on user login/logout from app. "Sign in..." text may be changed to
    name of logged in user. Bottom button shows either "Continue authorization" or "You are
    already logged in" information.
     */
    private fun updateUI() {


        when (cachedUserProfile) {
            null -> { // User is not logged in
                ApiClient.cachedCredentials = null
                ApiClient.cachedUserProfile = null

                loginSignInToStartTextView.text = getString(R.string.sign_in_to_start)
                loginLeftImageView.visibility = View.VISIBLE
                loginMiddleImageView.visibility = View.VISIBLE
                loginRightImageView.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_github))
                loginTextView.text = getString(R.string.continue_authorization)
            }
            else -> { // User was logged in
                ApiClient.cachedCredentials = cachedCredentials
                ApiClient.cachedUserProfile = cachedUserProfile

                loginSignInToStartTextView.text = cachedUserProfile!!.name
                loginLeftImageView.visibility = View.GONE
                loginMiddleImageView.visibility = View.GONE
                loginRightImageView.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_mood_good))
                loginTextView.text = getString(R.string.you_are_already_logged_in)
            }
        }
    }

    /*
    This function initializes logging in with auth0 / Google / Github via browser. If logging
    in is succesfull, then user profile data is downloaded. But at first it finally chceks GPS
    permissions which are necessery for app to start.
     */
    private fun loginWithBrowser() {

        // Final check GPS permissions
        if(!hasGpsPermissions()){
            checkGpsPermissions()
            return
        }

        // Login with browser.
        WebAuthProvider.login(account)
            .withScheme(getString(R.string.com_auth0_scheme))
            .withScope(getString(R.string.auth0_scope))
            .withAudience(getString(R.string.auth0_audience_0))
            .withAudience(getString(R.string.auth0_audience_1))
            .start(this, object : Callback<Credentials, AuthenticationException> {
                override fun onFailure(error: AuthenticationException) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Failure: ${error.getCode()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onSuccess(result: Credentials) {
                    cachedCredentials = result
                    downloadUserProfileData()
                }
            })
    }

    /*
    This function initialzies logging out by user. It is possible only after user was logged before.
     */
    private fun logout() {
        WebAuthProvider.logout(account)
            .withScheme(getString(R.string.com_auth0_scheme))
            .start(this, object : Callback<Void?, AuthenticationException> {
                override fun onFailure(error: AuthenticationException) {}

                override fun onSuccess(result: Void?) {
                    cachedCredentials = null
                    cachedUserProfile = null
                    updateUI()
                }
            })
    }

    /*
    This function downloads users profile data. User must be already logged in! If data is
    properly downloaded, then UI is updated and user is moved to GeneralActivity.
     */
    private fun downloadUserProfileData() {
        val client = AuthenticationAPIClient(account)
        client.userInfo(cachedCredentials!!.accessToken)
            .start(object : Callback<UserProfile, AuthenticationException> {
                override fun onFailure(error: AuthenticationException) {}

                override fun onSuccess(result: UserProfile) {
                    cachedUserProfile = result
                    updateUI()
                    registerUserAndGoToGeneral()
                }
            })
    }

    /*
    This function registers user on server. User data is put to next activity.
     */
    private fun registerUserAndGoToGeneral() {
        ApiClient.runRequestWithParser(
            path = "api/users/register",
            method = "POST",
            modelClass = Models.FullUser::class.java
        )
        { fullUser ->
            val generalIntent = Intent(this, GeneralActivity::class.java)
            generalIntent.putExtra(Consts.USER, fullUser)
            startActivity(generalIntent)
        }
    }

    /*
    This function initializes checking for location (GPS) permissions.
     */
    private fun checkGpsPermissions() {
        when (hasGpsPermissions()) {
            true -> return
            false -> requestGpsPermission()
        }
    }

    /*
    This function initializes checking for battery optimalization action.
     */
    @SuppressLint("BatteryLife")
    private fun batteryOptimization() {
        val intent = Intent()
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            intent.action = ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = Uri.parse("package:" + "com.example.roverondo_mobile")
            startActivity(intent)
        }
    }

    /*
    This function checks location (GPS) permissions according to Android API version.
     */
    @SuppressLint("InlinedApi")
    fun hasGpsPermissions(): Boolean {
        return when (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            true -> EasyPermissions.hasPermissions(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
            false -> EasyPermissions.hasPermissions(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    /*
    This function requests location (GPS) permissions according to Android API version.
     */
    @SuppressLint("InlinedApi")
    fun requestGpsPermission() {
        when (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            true -> EasyPermissions.requestPermissions(
                this,
                getString(R.string.please_grant_permission),
                Consts.CODE_GPS_PERMISSION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
            false -> EasyPermissions.requestPermissions(
                this,
                getString(R.string.please_grant_permission),
                Consts.CODE_GPS_PERMISSION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    /*
    What happens when there are permissions results
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    /*
    What happens when there permission is granted
     */
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

    /*
    What happens when permission is not granted
     */
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            checkGpsPermissions()
        }
    }

    /*
    This function needs to be overriden for Calligraphy to properly work.
    It changes all fonts in this activity to Almarai Light
     */
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    /*
    This function is overriden because app should just close after pressing back button in
    login screen.
     */
    override fun onBackPressed() {
        exitProcess(0)
    }
}
