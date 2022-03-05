package com.example.roverondo_mobile.adapters

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import com.example.roverondo_mobile.R
import com.example.roverondo_mobile.activities.GeneralActivity
import com.example.roverondo_mobile.api.ApiClient
import com.example.roverondo_mobile.fragments.WallFragment
import com.example.roverondo_mobile.models.Models
import com.example.roverondo_mobile.utils.*
import com.example.roverondo_mobile.utils.UtilsUI.loadCircularImage
import com.google.android.gms.maps.MapsInitializer
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody


class PostAdapter(
    context: GeneralActivity,
    private val wallFragment: WallFragment,
    private var commentsLazyLoader: CommentsLazyLoader? = null
) : AbstractAdapter<Models.Post, Holders.PostHolder>(context) {

    /*
    This is main function for posts. It initializes them and uses proper init function
    according to the post type.
     */
    @SuppressLint("CutPasteId")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        // Init
        val data = dataSource[position]
        var view = convertView
        val holder: Holders.PostHolder?

        // Init view and holder
        when (view == null) {
            false -> holder = view.tag as Holders.PostHolder
            true -> {

                // Finding proper views
                view = inflater.inflate(R.layout.row_post, parent, false)
                holder = Holders.PostHolder(
                    view.findViewById(R.id.postUserTextView),
                    view.findViewById(R.id.postUserImageView),
                    view.findViewById(R.id.postPostDateTextView),
                    view.findViewById(R.id.postMapView),
                    view.findViewById(R.id.postTypeTextView),
                    view.findViewById(R.id.postTypeImageView),
                    view.findViewById(R.id.postTitleTextView),
                    view.findViewById(R.id.postThumbUpImageView),
                    view.findViewById(R.id.postReactionsTextView),
                    view.findViewById(R.id.postCommentsNumberTextView),
                    view.findViewById(R.id.postCommentsImageView),
                    view.findViewById(R.id.postDescriptionTextView),
                    view.findViewById(R.id.postTopLeftTextView),
                    view.findViewById(R.id.postTopLeftImageView),
                    view.findViewById(R.id.postTopRightTextView),
                    view.findViewById(R.id.postTopRightImageView),
                    view.findViewById(R.id.postBottomLeftTextView),
                    view.findViewById(R.id.postBottomLeftImageView),
                    view.findViewById(R.id.postBottomRightTextView),
                    view.findViewById(R.id.postBottomRightImageView),
                    view.findViewById(R.id.postBottomSectionLinearLayout),
                    view.findViewById(R.id.postBackgroundBlurLayout)
                )

                // Init horizontal auto-scroll on text views
                (view.findViewById(R.id.postBottomLeftTextView) as TextView).isSelected = true
                (view.findViewById(R.id.postBottomRightTextView) as TextView).isSelected = true
                (view.findViewById(R.id.postTopLeftTextView) as TextView).isSelected = true
                (view.findViewById(R.id.postTopRightTextView) as TextView).isSelected = true

                view.tag = holder
            }
        }

        return initRow(holder, view!!, data)
    }

    /*
    This function initializes post -> sets up UI data, listeners and map.
     */
    @SuppressLint("SetTextI18n")
    override fun initRow(holder: Holders.PostHolder, view: View, data: Models.Post): View {

        // Top section: author name
        holder.userTextView.text = UtilsUI.parseNullString(data.user.nickname)

        // Top section: author image
        holder.userImageView.loadCircularImage(data.user)

        // Top section: created at time
        holder.postDateTextView.text =
            "Posted at: " + UtilsDates.timestampToLongDate(data.createdAt)

        // Top section: listeners
        holder.userTextView.setOnClickListener { context.openAnyUserProfile(data.user.id) }
        holder.userImageView.setOnClickListener { context.openAnyUserProfile(data.user.id) }

        // Middle section: map
        initMap(holder, data)

        // Title
        holder.titleTextView.text = UtilsUI.parseNullString(data.title)

        // Bottom section: reactions count
        holder.reactionsTextView.text = UtilsUI.parseNullString(data.reactionsCount)

        // Bottom section: comments count
        holder.commentsTextView.text = UtilsUI.parseNullString(data.commentsCount)

        // Bottom section: already liked
        when (data.alreadyReactedTo) {
            true -> holder.thumbUpImageView.setColorFilter(Color.GREEN)
            false -> holder.thumbUpImageView.setColorFilter(context.getColor(R.color.AppGray))
        }

        // Bottom section: description
        holder.descriptionTextView.text = UtilsUI.parseNullString(data.description)

        when (data.postType) {
            Consts.TYPE_WORKOUT -> updateWorkoutPost(holder, data)
            Consts.TYPE_ROUTE -> updateRoutePost(holder, data)
            else -> updateEventPost(holder, data)
        }

        // Bottom section listeners
        holder.bottomSection.setOnClickListener {
            context.openDetailsFragment(data.postType, data)
        }

        holder.bottomSection.setOnLongClickListener {
            if (data.user.id == context.currentUser.id) {
                UtilsAlerts.alertDialogWithFunction("Delete this post?") {
                    ApiClient.runRequestWithoutParser(
                        path = "api/posts/${data.id}",
                        method = "DELETE"
                    ) {
                        context.runOnUiThread {
                            dataSource.remove(data)
                            val wallFragment =
                                context.supportFragmentManager.findFragmentByTag(Consts.FRAGMENT)
                            if (wallFragment is WallFragment)
                                wallFragment.lazyLoader.removeOne()
                            notifyDataSetChanged()
                            UtilsAlerts.snackbar("Removed post: ${data.title}")
                        }
                    }
                }
            } else {
                UtilsAlerts.snackbar("No action possible.")
            }
            true
        }

        holder.thumbUpImageView.setOnClickListener {
            when (data.alreadyReactedTo) {
                true -> unlikePost(holder, data)
                false -> likePost(holder, data)
            }
        }

        holder.commentsImageView.setOnClickListener { openCommentsDialogWindow(data, holder) }

        // Blur
        UtilsUI.addBlur(holder.blurView, context)

        return view
    }

    /*
    This function updates / sets up workout post. There are 4 data fields.
     */
    @SuppressLint("SetTextI18n")
    private fun updateWorkoutPost(holder: Holders.PostHolder, data: Models.Post) {

        // Type
        holder.typeTextView.text = context.getString(R.string.new_workout)
        holder.typeImageView.setImageDrawable(
            AppCompatResources.getDrawable(
                context,
                R.drawable.ic_explore
            )
        )

        // Display bottom section top left and top right
        holder.topLeftTextView.visibility = View.VISIBLE
        holder.topLeftImageView.visibility = View.VISIBLE
        holder.topRightTextView.visibility = View.VISIBLE
        holder.topRightImageView.visibility = View.VISIBLE

        // Change drawables
        holder.topLeftImageView.setImageDrawable(
            AppCompatResources.getDrawable(
                context,
                R.drawable.ic_timer
            )
        )
        holder.topRightImageView.setImageDrawable(
            AppCompatResources.getDrawable(
                context,
                R.drawable.ic_speed
            )
        )

        // Init
        val workout = data.workout!!
        val route = data.workout.route

        // Top left
        holder.topLeftTextView.text =
            UtilsDates.differenceBetweenTimestamps(workout.startTime!!, workout.endTime!!)

        // Top right
        holder.topRightTextView.text = UtilsUI.parseNullToKilometersPerMeters(workout.averageSpeed)

        // Bottom left
        holder.bottomLeftTextView.text = route.location

        // Bottom right
        holder.bottomRightTextView.text = UtilsUI.parseNullToKilometers(route.distance)
    }

    /*
    This function updates / sets up route post. There are 2 data fields.
     */
    @SuppressLint("SetTextI18n")
    private fun updateRoutePost(holder: Holders.PostHolder, data: Models.Post) {

        // Type
        holder.typeTextView.text = context.getString(R.string.new_route)
        holder.typeImageView.setImageDrawable(
            AppCompatResources.getDrawable(
                context,
                R.drawable.ic_bike
            )
        )

        // Hide bottom section top left and top right
        holder.topLeftTextView.visibility = View.GONE
        holder.topLeftImageView.visibility = View.GONE
        holder.topRightTextView.visibility = View.GONE
        holder.topRightImageView.visibility = View.GONE

        // Bottom left
        holder.bottomLeftTextView.text = data.plannedRoute?.route?.location
        holder.bottomLeftTextView.isSelected = true

        // Bottom right
        holder.bottomRightTextView.text =
            UtilsUI.parseNullToKilometers(data.plannedRoute?.route?.distance!!)
    }

    /*
    This function updates / sets up event post. There are 4 data fields.
     */
    @SuppressLint("SetTextI18n")
    private fun updateEventPost(holder: Holders.PostHolder, data: Models.Post) {

        // Type
        holder.typeTextView.text = context.getString(R.string.new_event)
        holder.typeImageView.setImageDrawable(
            AppCompatResources.getDrawable(
                context,
                R.drawable.ic_event
            )
        )

        // Display bottom section top left and top right
        holder.topLeftTextView.visibility = View.VISIBLE
        holder.topLeftImageView.visibility = View.VISIBLE
        holder.topRightTextView.visibility = View.VISIBLE
        holder.topRightImageView.visibility = View.VISIBLE

        // Change drawables
        holder.topLeftImageView.setImageDrawable(
            AppCompatResources.getDrawable(
                context,
                R.drawable.ic_date
            )
        )
        holder.topRightImageView.setImageDrawable(
            AppCompatResources.getDrawable(
                context,
                R.drawable.ic_time
            )
        )

        // Init
        val route = data.plannedRoute!!.route
        val dateAndTime = UtilsUI.parseNullDatetimeString(data.startsAt).split(" ")

        // Top left
        holder.topLeftTextView.text = dateAndTime.first()

        // Top right
        holder.topRightTextView.text = dateAndTime.last()

        // Bottom left
        holder.bottomLeftTextView.text = route.location

        // Bottom right
        holder.bottomRightTextView.text = UtilsUI.parseNullToKilometers(route.distance)
    }

    /*
    This requests API to like post (add reaction).
     */
    private fun likePost(holder: Holders.PostHolder, data: Models.Post) {

        // Init
        val reactionsBefore = (holder.reactionsTextView.text.toString().replace(" ", "")).toInt()

        // Create body
        val mediaType = "application/json".toMediaTypeOrNull()
        val reaction = Models.Reaction(
            postId = data.id!!,
            userId = context.currentUser.id
        )
        val bodyJson = Gson().toJson(reaction)
        val body = bodyJson.toRequestBody(mediaType)

        // Make request
        ApiClient.runRequestWithoutParser(
            path = "api/posts/reactions",
            method = "POST",
            body = body
        ) {
            // Update UI
            context.runOnUiThread {
                data.alreadyReactedTo = true
                holder.thumbUpImageView.setColorFilter(Color.GREEN)
                holder.reactionsTextView.text = (reactionsBefore + 1).toString()
                data.reactionsCount = data.reactionsCount!! + 1
            }
        }
    }

    /*
    This requests API to unlike post (remove reaction).
     */
    private fun unlikePost(holder: Holders.PostHolder, data: Models.Post) {

        // Init
        val reactionsBefore = (holder.reactionsTextView.text.toString().replace(" ", "")).toInt()

        // Make request
        ApiClient.runRequestWithoutParser(
            path = "api/posts/reactions?user=${context.currentUser.id}&post=${data.id}",
            method = "DELETE"
        ) {
            // Update UI
            context.runOnUiThread {
                data.alreadyReactedTo = false
                holder.thumbUpImageView.setColorFilter(context.getColor(R.color.AppGray))
                holder.reactionsTextView.text = (reactionsBefore - 1).toString()
                data.reactionsCount = data.reactionsCount!! - 1
            }
        }
    }

    /*
    This opens dialog window and displays comments for given post.
     */
    private fun openCommentsDialogWindow(post: Models.Post, holder: Holders.PostHolder) {

        // Init dialog
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_comments)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        UtilsUI.addBlur(dialog.findViewById(R.id.dialogCommentsBackgroundBlurLayout), context)

        // Init adapter
        val listView = dialog.findViewById(R.id.dialogCommentsListView) as ListView
        val adapter = CommentAdapter(context, dialog)
        listView.adapter = adapter
        adapter.notifyDataSetChanged()
        commentsLazyLoader = CommentsLazyLoader(adapter, post.id!!)
        listView.setOnScrollListener(commentsLazyLoader)
        adapter.notifyDataSetChanged()

        // Listeners
        val addImageView = dialog.findViewById(R.id.dialogCommentsAddImageView) as ImageView
        addImageView.setOnClickListener {
            addComment(dialog, holder, post)
            context.hideKeyboard()
        }

        listView.setOnItemLongClickListener { parent, view, position, id ->
            deleteComment(dialog, holder, post, adapter.dataSource[position])
            true
        }

        // Set dialog window wider
        dialog.show()
    }

    /*
    This initializes adding new comment. At first it checks whether text field contains
    proper new comment value, and if it does, API is requested to add new comment. Then list
    of all comments is updated.
     */
    private fun addComment(dialog: Dialog, holder: Holders.PostHolder, post: Models.Post) {

        val commentLayout =
            dialog.findViewById(R.id.dialogCommentsTextInputLayout) as TextInputLayout
        val commentTextField =
            dialog.findViewById(R.id.dialogCommentsTextInputEditText) as TextView
        val newCommentText = commentTextField.text.toString()

        when (newCommentText.isEmpty()) {
            true -> {
                commentLayout.error = "Comment too short"
                commentLayout.isErrorEnabled = true
                commentLayout.setErrorIconDrawable(R.drawable.ic_error)
                commentLayout.boxStrokeColor = Color.RED
            }
            false -> {
                commentLayout.isErrorEnabled = false
                commentLayout.boxStrokeColor = context.getColor(R.color.AppGray)
                addCommentApiRequest(newCommentText, commentTextField, dialog, holder, post)
            }
        }
    }

    /*
    This initializes deleting comment procedure. At firtst it checks whether post belongs to user.
    Then it opens dialog witndow with delete info. At the end it makes API DELETE call, and updates
    list and number of comments in post.
     */
    private fun deleteComment(
        dialog: Dialog,
        holder: Holders.PostHolder,
        post: Models.Post,
        comment: Models.Comment
    ) {
        if (comment.user.id == context.currentUser.id) {
            UtilsAlerts.alertDialogWithFunction("Delete this comment?") {
                ApiClient.runRequestWithoutParser(
                    path = "api/comments/${comment.id}",
                    method = "DELETE"
                ) {
                    context.runOnUiThread {
                        val listView = dialog.findViewById(R.id.dialogCommentsListView) as ListView
                        val adapter = listView.adapter as CommentAdapter
                        commentsLazyLoader?.reset()
                        adapter.dataSource.clear()
                        adapter.notifyDataSetChanged()
                        post.commentsCount = post.commentsCount!! - 1
                        holder.commentsTextView.text = post.commentsCount.toString()
                    }
                }
            }
        } else {
            UtilsAlerts.snackbar("No action possible.")
        }
    }

    /*
    This function calls API to add a comment to given post.
     */
    private fun addCommentApiRequest(
        newCommentText: String,
        commentTextField: TextView,
        dialog: Dialog,
        holder: Holders.PostHolder,
        post: Models.Post
    ) {

        // Create request body
        val gson = Gson()
        val mediaType = "application/json".toMediaTypeOrNull()
        val newComment = Models.CommentToPost(
            postId = post.id!!,
            userId = context.currentUser.id,
            text = newCommentText
        )
        val bodyJson = gson.toJson(newComment)
        val body = bodyJson.toRequestBody(mediaType)

        // Make request
        ApiClient.runRequestWithoutParser(
            path = "api/comments",
            method = "POST",
            body = body
        ) {
            context.runOnUiThread {
                commentTextField.text = ""
                val listView = dialog.findViewById(R.id.dialogCommentsListView) as ListView
                val adapter = listView.adapter as CommentAdapter
                commentsLazyLoader?.reset()
                adapter.dataSource.clear()
                adapter.notifyDataSetChanged()
                post.commentsCount = post.commentsCount!! + 1
                holder.commentsTextView.text = post.commentsCount.toString()
            }
        }
    }

    /*
    This function initializes maps with route points on it.
     */
    private fun initMap(holder: Holders.PostHolder, data: Models.Post) {

        // Init data
        val routePoints = when (data.postType) {
            Consts.TYPE_WORKOUT -> data.workout?.route?.route
            Consts.TYPE_ROUTE -> data.plannedRoute?.route?.route
            else -> data.plannedRoute?.route?.route
        }

        // Init map and add route points
        when (holder.googleMap) {
            null -> {
                holder.mapView.onCreate(null)
                holder.mapView.onResume()
                holder.mapView.getMapAsync {
                    MapsInitializer.initialize(context)
                    holder.googleMap = it
                    UtilsMaps.addRouteToMap(holder.googleMap!!, routePoints)
                }
            }
            else -> {
                UtilsMaps.addRouteToMap(holder.googleMap!!, routePoints)
            }
        }
    }

    /*
    When data soruce in adapter is changed, and is greater than zero, than loading indicator is
    removed. If there are no posts - then there is "Empty" text view in the middle of screen.
     */
    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()
        if (dataSource.size == 0) {
            wallFragment.addEmptyTextView()
        }
    }
}