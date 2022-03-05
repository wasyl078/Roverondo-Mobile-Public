package com.example.roverondo_mobile.api

import android.util.Log
import com.auth0.android.result.Credentials
import com.auth0.android.result.UserProfile
import com.example.roverondo_mobile.models.Models
import com.example.roverondo_mobile.utils.Consts
import com.example.roverondo_mobile.utils.UtilsAlerts
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

object ApiClient {

    // Variables & consts
    var cachedCredentials: Credentials? = null
    var cachedUserProfile: UserProfile? = null
    var gson = Gson()

    /*
    This function creates request to API. Returned response is parsed to given class and
    that parsed response is then used as an argument for additional function.
     */
    fun <T> runRequestWithParser(
        path: String,
        method: String,
        body: RequestBody? = null,
        modelClass: Class<T>,
        onResponseFunction: ((T) -> Unit)
    ) {
        Log.d("API", cachedCredentials!!.accessToken)
        Log.d("API", "$method $path")
        UtilsAlerts.addIndicator()

        // Init options
        val fullPath = "${Consts.IP_ADDRESS}$path"
        val token = "${Consts.TOKEN_TYPE} ${cachedCredentials!!.accessToken}"
        val parsedBody: RequestBody? = if (method == "POST" && body == null)
            "".toRequestBody()
        else
            body

        // Init request
        val request = Request.Builder()
            .method(method, parsedBody)
            .addHeader(Consts.AUTHORIZATION, token)
            .url(fullPath)
            .build()

        // Make request
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                UtilsAlerts.snackbar("Failure! Could not connect to API!")
                UtilsAlerts.removeIndicator()
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.code > 300) {
                    UtilsAlerts.snackbar("Failure - code ${response.code}")
                    UtilsAlerts.removeIndicator()
                } else {
                    onResponseFunction(gson.fromJson(response.body!!.string(), modelClass))
                    UtilsAlerts.removeIndicator()
                }
            }
        })
    }

    /*
    This function creates request to API. Returned response is not parsed and after successfull
    response additional function is called.
     */
    fun runRequestWithoutParser(
        path: String,
        method: String,
        body: RequestBody? = null,
        onWrongCodeFunction: (() -> Unit)? = null,
        onResponseFunction: (() -> Unit)? = null
    ) {
        Log.d("API", "$method $path")
        UtilsAlerts.addIndicator()

        // Init options
        val fullPath = "${Consts.IP_ADDRESS}$path"
        val token = "${Consts.TOKEN_TYPE} ${cachedCredentials!!.accessToken}"
        val parsedBody: RequestBody? = if (method == "POST" && body == null)
            "".toRequestBody()
        else
            body

        // Init request
        val request = Request.Builder()
            .method(method, parsedBody)
            .addHeader(Consts.AUTHORIZATION, token)
            .url(fullPath)
            .build()

        // Make request
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                UtilsAlerts.removeIndicator()
                UtilsAlerts.snackbar("Failure! Could not connect to API!")
            }

            override fun onResponse(call: Call, response: Response) {
                UtilsAlerts.removeIndicator()
                if (response.code > 300) {
                    UtilsAlerts.snackbar("Failure - code ${response.code}")
                    if (onWrongCodeFunction != null)
                        onWrongCodeFunction()
                } else {
                    if (onResponseFunction != null)
                        onResponseFunction()
                }
            }
        })
    }

    /*
    This function creates request to Open Request API. It calls for route between two points.
     */
    fun runRequestOpenRoute(
        latlngStart: LatLng,
        latlngEnd: LatLng,
        onResponseFunction: ((Models.OpenRouteTrack) -> Unit)
    ) {

        UtilsAlerts.addIndicator()

        // Init
        val apiKey = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
        val basePath = "https://api.openrouteservice.org/v2/directions/cycling-regular"
        val fullPath =
            "$basePath?api_key=$apiKey&start=${latlngStart.longitude},${latlngStart.latitude}&end=${latlngEnd.longitude},${latlngEnd.latitude}"

        // Creaete request
        val request = Request.Builder()
            .method("GET", null)
            .url(fullPath)
            .build()

        // Make request
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                UtilsAlerts.removeIndicator()
                UtilsAlerts.snackbar("Failure! Could not connect to Open Route API!")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code > 300) {
                    UtilsAlerts.snackbar("Failure - code ${response.code}")
                    UtilsAlerts.removeIndicator()
                } else {
                    onResponseFunction(
                        gson.fromJson(
                            response.body!!.string(),
                            Models.OpenRouteTrack::class.java
                        )
                    )
                    UtilsAlerts.removeIndicator()
                }
            }
        })
    }

    /*
    This function creates request to Open Elevation API. It calls for points elevatiosn.
     */
    fun runRequestOpenElevationExtended(
        points: List<Models.Point>,
        onResponseFunction: ((Models.OpenElevation) -> Unit)
    ) {

        UtilsAlerts.addIndicator()

        // Init
        val path = "https://api.open-elevation.com/api/v1/lookup"
        var globalCounter = 0
        val responsesHashMap = hashMapOf<Int, Models.OpenElevation?>()

        val chunkSize = (points.size / (points.size / 200 + 1))
        val chunkedPoints = points.chunked(chunkSize)

        // Fill with nulls
        chunkedPoints.forEachIndexed { idx, _ -> responsesHashMap[idx] = null }

        // Set up many requests
        chunkedPoints.forEachIndexed { idx, chunk ->

            // Create body
            val mediaType = "application/json".toMediaTypeOrNull()
            val locations = chunk.map {
                Models.OpenElevationElevation(
                    latitude = it.latitude,
                    longitude = it.longitude
                )
            }

            val postLocation = Models.OpenPostElevation(locations = locations)
            val bodyJson = gson.toJson(postLocation)
            val body = bodyJson.toRequestBody(mediaType)

            // Creaete request
            val request = Request.Builder()
                .method("POST", body)
                .url(path)
                .build()

            // Make request
            OkHttpClient().newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    UtilsAlerts.snackbar("Failure! Could not connect to Open Elevation API!")
                    UtilsAlerts.removeIndicator()
                    return
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.code > 300) {
                        UtilsAlerts.snackbar("Failure - code ${response.code}")
                        UtilsAlerts.removeIndicator()
                    } else {
                        val responseString = response.body!!.string()
                        responsesHashMap[idx] =
                            gson.fromJson(responseString, Models.OpenElevation::class.java)
                        globalCounter += 1
                    }
                }
            })
        }

        // Run 5 seconds while loop to get all elevations
        Thread {
            var counter = 0
            while (counter < 5) {

                // Wait
                Thread.sleep(1000)

                // Check whether all responses are finished
                if (globalCounter == responsesHashMap.size) {

                    // Connect all results
                    val connectedResults = ArrayList<Models.OpenElevationElevation>()
                    for (i in 0 until responsesHashMap.size) {
                        connectedResults.addAll(responsesHashMap[i]!!.results)
                    }

                    // Call response function
                    onResponseFunction(Models.OpenElevation(connectedResults))

                    // Break loop and thread, stop loading animation
                    UtilsAlerts.removeIndicator()
                    return@Thread
                }
                counter += 1
            }
            UtilsAlerts.snackbar("Failure! Could not connect to Open Elevation API!")
            UtilsAlerts.removeIndicator()
        }.start()
    }
}
