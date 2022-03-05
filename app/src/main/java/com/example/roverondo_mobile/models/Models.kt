package com.example.roverondo_mobile.models

import java.io.Serializable

class Models {

    data class FullUser(
        val id: Int,
        val providerId: String,
        var nickname: String,
        val email: String?,
        val profilePicture: String?,
        var city: String?,
        var bio: String?,
        var birthDate: String?,
        var gender: String?,
        var weight: String?,
        val maxHeartRate: String?,
        val dateJoined: String,
        val totalDistance: Float?,
        val timeInMotion: Float?,
        val followers: Int?,
        val following: Int?,
        val totalReceivedReactions: Int?,
        val totalGivenReactions: Int?,
        var statistics: UsersStatistics?
    ) : Serializable

    data class UsersStatistics(
        val totalDistanceTravelled: Int,
        val timeInMotion: Int,
        val followers: Int,
        val followings: Int,
        val totalGivenReactions: Int,
        val totalReceivedReactions: Int
    ) : Serializable

    data class ChartsPoint(
        val from: String,
        val to: String,
        val distance: Float,
        val elevation: Float,
        val averageSpeed: Float,
        val activities: Int
    ) : Serializable

    data class Post(
        val id: Int?,
        val title: String,
        val description: String,
        val createdAt: String,
        val modifiedAt: String?,
        val postType: String,
        var commentsCount: Int?,
        var reactionsCount: Int?,
        val user: FullUser,
        var alreadyReactedTo: Boolean?,
        val plannedRoute: PlannedRoute?,
        val route: Route?,
        val workout: Workout?,
        val visibility: String = "PUBLIC",
        val startsAt: String?,
        var alreadyJoined: Boolean?
    ) : Serializable

    data class PlannedRoute(
        val id: Int,
        val title: String,
        val description: String?,
        val modifiedAt: String?,
        val createdAt: String,
        val route: Route
    ) : Serializable

    data class Workout(
        val id: Int?,
        val startTime: String?,
        val endTime: String?,
        val averageSpeed: Float?,
        val maxSpeed: Float?,
        val minAtmosphericPressure: Float?,
        val avgAtmosphericPressure: Float?,
        val maxAtmosphericPressure: Float?,
        val calories: Float?,
        val route: Route
    ) : Serializable

    data class Route(
        val distance: Float?,
        val elevation: Float?,
        val minAltitude: Float?,
        val avgAltitude: Float?,
        val maxAltitude: Float?,
        val location: String?,
        val route: List<Point>
    ) : Serializable

    data class EventToPost(
        val title: String,
        val description: String,
        val visibility: String = "FOLLOWERS_ONLY",
        val startsAt: String,
    ) : Serializable

    data class PlannedRouteToPost(
        val id: Int? = null,
        val title: String,
        val description: String,
        val points: List<Point>
    ) : Serializable

    data class PlannedRouteToPostPost(
        val title: String,
        val description: String,
        val visibility: String = "FOLLOWERS_ONLY"
    ) : Serializable

    data class WorkoutToPost(
        val title: String,
        val description: String,
        val visibility: String = "FOLLOWERS_ONLY",
        val route: List<Point>
    ) : Serializable

    data class Point(
        val longitude: Float,
        val latitude: Float,
        var elevation: Float,
        val speed: Float?,
        val pressure: Float?,
        val location: String? = null,
        val timestamp: String? = null
    ) : Serializable

    data class Reaction(
        val postId: Int,
        val userId: Int
    ) : Serializable

    data class ReactionToComment(
        val commentId: Int,
        val userId: Int
    ) : Serializable

    data class Comment(
        val id: Int,
        val text: String,
        val createdAt: String,
        val modifiedAt: String?,
        val user: FullUser,
        var reactions: Int,
        var alreadyReactedTo: Boolean
    ) : Serializable

    data class CommentToPost(
        val postId: Int,
        val userId: Int,
        val text: String
    ) : Serializable

    data class OpenRouteTrack(
        val features: List<OpenRouteFeatures>
    ) : Serializable

    data class OpenRouteFeatures(
        val geometry: OpenRouteGeometry
    ) : Serializable

    data class OpenRouteGeometry(
        val coordinates: List<List<Float>>
    ) : Serializable

    data class OpenElevation(
        val results: List<OpenElevationElevation>
    ) : Serializable

    data class OpenPostElevation(
        val locations: List<OpenElevationElevation>
    ) : Serializable

    data class OpenElevationElevation(
        val latitude: Float,
        val longitude: Float,
        var elevation: Int? = null
    ) : Serializable
}