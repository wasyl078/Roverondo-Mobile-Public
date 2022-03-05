package com.example.roverondo_mobile.utils

import com.google.android.gms.maps.model.LatLng

object Consts {

    // General
    const val FRAGMENT = "FRAGMENT"
    const val FOLLOW = "FOLLOW"
    const val FOLLOWERS = "FOLLOWERS"
    const val FOLLOWING = "FOLLOWING"
    const val TITLE = "TITLE"
    const val USER = "USER"
    const val ID = "ID"
    const val ID2 = "ID2"
    const val POST = "POST"
    const val ROUTE = "ROUTE"
    const val DISTANCE = "DISTANCE"
    const val POINTS = "POINTS"

    // Login
    const val LOGOUT_CODE = "LOGOUT_CODE"
    const val CODE_GPS_PERMISSION = 0

    // Universal
    const val NONE = "N/A"

    // Tracking
    const val SEA_LEVEL_PRESSURE = 1013.25
    const val DEFAULT_TEMPERATURE = 15

    // Edit (user profile)
    const val EDIT_BIO = "EDIT_BIO"
    const val EDIT_WEIGHT = "EDIT_WEIGHT"
    const val EDIT_CITY = "EDIT_CITY"
    const val MALE = "MALE"
    const val FEMALE = "FEMALE"
    const val NOT_SPECIFIED = "NOT_SPECIFIED"

    // Filters
    const val FILTER_ALL = "ActivityPost,PlannedRoutePost,EventPost"
    const val FILTER_WORKOUTS = "ActivityPost"
    const val FILTER_ROUTES = "PlannedRoutePost"
    const val FILTER_EVENTS = "EventPost"

    // Post adapter
    const val TYPE_WORKOUT = "ActivityPost"
    const val TYPE_ROUTE = "PlannedRoutePost"
    const val TYPE_EVENT = "EventPost"

    // Tracking service
    const val ACTION_START_SERVICE = "ACTION_START_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val NOTIFICATION_CHANNEL_ID = "NOTIFICATION_CHANNEL_TRACKING_ID"
    const val NOTIFICATION_CHANNEL_NAME = "NOTIFICATION_CHANNEL_TRACKING_NAME"
    const val NOTIFICATION_ID = 1
    const val LOCATION_UPDATE_INTERVAL = 5000L
    const val FASTEST_LOCATION_INTERVAL = 2000L

    // API Client
    const val IP_ADDRESS = "https://roverondo.link/"
    const val AUTHORIZATION = "Authorization"
    const val TOKEN_TYPE = "Bearer"

    // Settings
    const val GENERAL_ROUTE_TYPE = "GENERAL_ROUTE_TYPE"
    const val FOLLOW_ROUTE_TYPE = "FOLLOW_ROUTE_TYPE"

    // Set up
    val WROCLAW = LatLng(51.107883, 17.038538)

    // Event
    const val DATETAG = "datePicker"
    const val TIMETAG = "timePicker"
}