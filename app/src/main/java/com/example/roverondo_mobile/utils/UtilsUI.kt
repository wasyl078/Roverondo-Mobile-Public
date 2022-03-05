package com.example.roverondo_mobile.utils

import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.example.roverondo_mobile.R
import com.example.roverondo_mobile.models.Models
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump


object UtilsUI {

    /*
    This function sets up calligrapy. This function sets font in top to almarai light.
     */
    fun setCalligraphy() {
        ViewPump.init(
            ViewPump.builder()
                .addInterceptor(
                    CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                            .setFontAttrId(R.attr.fontPath)
                            .setDefaultFontPath("fonts/almarai_light.ttf")
                            .build()
                    )
                )
                .build()
        )
    }

    /*
    This function makes top and bottom bars transparent on device.
     */
    fun setTransparentTopBottomBards(activity: AppCompatActivity) {
        val window: Window = activity.window
        val backgroud: Drawable? =
            ContextCompat.getDrawable(
                activity,
                R.drawable.gradient_green_rectangle
            )
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(activity, android.R.color.transparent)
        window.navigationBarColor =
            ContextCompat.getColor(activity, android.R.color.transparent)
        window.setBackgroundDrawable(backgroud)
    }

    // Blur
    fun addBlur(blur: BlurView, activity: AppCompatActivity) {
        val decorView = activity.window.decorView
        val rootView = decorView.findViewById(android.R.id.content) as ViewGroup
        val windowBackground = decorView.background

        blur.setupWith(rootView)
            .setFrameClearDrawable(windowBackground)
            .setBlurAlgorithm(RenderScriptBlur(activity))
            .setBlurRadius(25f)
            .setBlurAutoUpdate(true)
            .setHasFixedTransformationMatrix(false)
    }

    /*
    This function helps in parsing null data to string.
     */
    fun <T> parseNullString(value: T?, extraString: String = ""): String {
        return when (value) {
            null -> Consts.NONE
            else -> "$value $extraString"
        }
    }

    /*
    This function helps in parsing null data to string.
     */
    fun parseNullToMeters(value: Number?): String {
        return when (value) {
            null -> Consts.NONE
            else -> String.format("%.2f m", value)
        }
    }

    /*
    This function helps in parsing null number data (meters) to distance in kilometers.
     */
    fun parseNullToKilometers(value: Number?): String {
        return when (value) {
            null -> Consts.NONE
            else -> {
                val kilometers = value.toFloat() / 1000
                String.format("%.3f km", kilometers)
            }
        }
    }

    /*
    This function helps in parsing null number data (meters per seconds) to kilometers per meters.
     */
    fun parseNullToKilometersPerMeters(value: Number?): String {
        return when (value) {
            null -> Consts.NONE
            else -> {
                val kilometersPerSeconds = value.toFloat() * 3.6
                String.format("%.3f km/h", kilometersPerSeconds)
            }
        }
    }

    /*
    This function helps in parsing null number data (seconds) to hours and minutes.
     */
    fun parseNullToHoursMinutes(value: Number?): String {
        return when (value) {
            null -> Consts.NONE
            else -> {
                val hours = (value.toFloat() / 3600).toInt()
                val minutes = ((value.toFloat() - 60 * hours) / 60).toInt()
                "$hours h $minutes min"
            }
        }
    }

    /*
    This function helps in parsing null data to date (string).
     */
    fun parseNullDateString(valueDate: String?): String {
        return when (valueDate) {
            null -> Consts.NONE
            else -> UtilsDates.timestampToShortDate(valueDate)
        }
    }

    /*
    This function helps in parsing null data to datetime (string).
     */
    fun parseNullDatetimeString(valueDate: String?): String {
        return when (valueDate) {
            null -> Consts.NONE
            else -> UtilsDates.timestampToLongDate(valueDate)
        }
    }

    // https://stackoverflow.com/questions/34037939/how-to-make-border-for-circle-cropped-image-in-glide
    fun ImageView.loadCircularImage(
        fullUser: Models.FullUser,
        borderSize: Float = 2F,
        borderColor: Int = Color.BLACK
    ) {
        val properModel = when (fullUser.profilePicture == null) {
            false -> fullUser.profilePicture
            true -> {
                val nick = fullUser.nickname
                    .replace(" ", "%20")
                    .replace(".", "%20")
                "https://eu.ui-avatars.com/api/?name=$nick&background=random"
            }
        }
        Glide.with(context)
            .asBitmap()
            .load(properModel)
            .placeholder(R.drawable.ic_cloud_download)
            .apply(RequestOptions.circleCropTransform())
            .into(object : BitmapImageViewTarget(this) {
                override fun setResource(resource: Bitmap?) {
                    setImageDrawable(
                        resource?.run {
                            RoundedBitmapDrawableFactory.create(
                                resources,
                                if (borderSize > 0) {
                                    createBitmapWithBorder(borderSize, borderColor)
                                } else {
                                    this
                                }
                            ).apply {
                                isCircular = true
                            }
                        }
                    )
                }
            })
    }


    private fun Bitmap.createBitmapWithBorder(borderSize: Float, borderColor: Int): Bitmap {
        val borderOffset = (borderSize * 2).toInt()
        val halfWidth = width / 2
        val halfHeight = height / 2
        val circleRadius = Math.min(halfWidth, halfHeight).toFloat()
        val newBitmap = Bitmap.createBitmap(
            width + borderOffset,
            height + borderOffset,
            Bitmap.Config.ARGB_8888
        )

        // Center coordinates of the image
        val centerX = halfWidth + borderSize
        val centerY = halfHeight + borderSize

        val paint = Paint()
        val canvas = Canvas(newBitmap).apply {
            // Set transparent initial area
            drawARGB(0, 0, 0, 0)
        }

        // Draw the transparent initial area
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, centerY, circleRadius, paint)

        // Draw the image
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(this, borderSize, borderSize, paint)

        // Draw the createBitmapWithBorder
        paint.xfermode = null
        paint.style = Paint.Style.STROKE
        paint.color = borderColor
        paint.strokeWidth = borderSize
        canvas.drawCircle(centerX, centerY, circleRadius, paint)
        return newBitmap
    }
}
