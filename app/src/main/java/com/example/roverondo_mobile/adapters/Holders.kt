package com.example.roverondo_mobile.adapters

import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import eightbitlab.com.blurview.BlurView

class Holders {

    data class PostHolder(
        var userTextView: TextView,
        var userImageView: ImageView,
        var postDateTextView: TextView,
        var mapView: MapView,
        var typeTextView: TextView,
        var typeImageView: ImageView,
        var titleTextView: TextView,
        var thumbUpImageView: ImageView,
        var reactionsTextView: TextView,
        var commentsTextView: TextView,
        var commentsImageView: ImageView,
        var descriptionTextView: TextView,
        var topLeftTextView: TextView,
        var topLeftImageView: ImageView,
        var topRightTextView: TextView,
        var topRightImageView: ImageView,
        var bottomLeftTextView: TextView,
        var bottomLeftImageView: ImageView,
        var bottomRightTextView: TextView,
        var bottomRightImageView: ImageView,
        var bottomSection: LinearLayout,
        var blurView: BlurView,
        var googleMap: GoogleMap? = null
    )

    data class CommentHolder(
        val userTextView: TextView,
        val userImageView: ImageView,
        val dateTextView: TextView,
        val contentTextView: TextView,
        val reactionTextView: TextView,
        val reactionImageView: ImageView,
        val blurView: BlurView
    )

    data class UserHolder(
        val userTextView: TextView,
        val userImageView: ImageView,
        var blurView: BlurView
    )

    data class UserHolderAward(
        val userTextView: TextView,
        val userImageView: ImageView,
        val awardImageView: ImageView,
        var blurView: BlurView,
        var position: Int = -1
    )

    data class PointHolder(
        val pointNumberTextView: TextView,
        val pointDataTextView: TextView,
        val blurView: BlurView,
        var position: Int = -1
    )

    data class RouteHolder(
        val titleTextView: TextView,
        val descriptionTextView: TextView,
        val locationTextView: TextView,
        val distanceTextView: TextView,
        val dateTextView: TextView,
        val reactionsTextView: TextView,
        val blurView: BlurView
    )
}