package com.example.roverondo_mobile.adapters

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import com.example.roverondo_mobile.R
import com.example.roverondo_mobile.activities.GeneralActivity
import com.example.roverondo_mobile.api.ApiClient
import com.example.roverondo_mobile.models.Models
import com.example.roverondo_mobile.utils.UtilsDates
import com.example.roverondo_mobile.utils.UtilsUI
import com.example.roverondo_mobile.utils.UtilsUI.loadCircularImage
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody


class CommentAdapter(
    context: GeneralActivity,
    val dialog: Dialog,
) : AbstractAdapter<Models.Comment, Holders.CommentHolder>(context) {

    /*
    This is main function for comments. It initializes them using View Holder pattern.
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        // Init
        val data = dataSource[position]
        var view = convertView
        val holder: Holders.CommentHolder?

        // Init view and holder
        when (view == null) {
            false -> holder = view.tag as Holders.CommentHolder
            true -> {
                view = inflater.inflate(R.layout.row_comment, parent, false)
                holder = Holders.CommentHolder(
                    view.findViewById(R.id.rowCommentAuthorTextView),
                    view.findViewById(R.id.rowCommentAuthorImageView),
                    view.findViewById(R.id.rowCommentDateTextView),
                    view.findViewById(R.id.rowCommentContentTextView),
                    view.findViewById(R.id.rowCommentReactionTextView),
                    view.findViewById(R.id.rowCommentReactionImageView),
                    view.findViewById(R.id.rowCommentBackgroundBlurLayout),
                )
                view.tag = holder
            }
        }

        return initRow(holder, view!!, data)
    }

    /*
    This function initializes comment -> sets up UI data and listeners.
     */
    @SuppressLint("SetTextI18n")
    override fun initRow(
        holder: Holders.CommentHolder,
        view: View,
        data: Models.Comment
    ): View {

        // Author image
        holder.userImageView.loadCircularImage(data.user)

        // Author text
        holder.userTextView.text = data.user.nickname

        // Date
        holder.dateTextView.text = "Added at: ${UtilsDates.timestampToLongDate(data.createdAt)}"

        // Content
        holder.contentTextView.text = data.text

        // Reactions count
        holder.reactionTextView.text = data.reactions.toString()

        // Reactions button
        when (data.alreadyReactedTo) {
            false -> holder.reactionImageView.setColorFilter(context.getColor(R.color.AppLightGray))
            true -> holder.reactionImageView.setColorFilter(Color.GREEN)
        }

        // Reactions listener
        holder.reactionImageView.setOnClickListener {
            when (data.alreadyReactedTo) {
                true -> unlikeComment(holder, data)
                false -> likeComment(holder, data)
            }
        }

        // Author listener
        holder.userTextView.setOnClickListener {
            context.openAnyUserProfile(data.user.id)
            dialog.dismiss()
        }

        holder.userImageView.setOnClickListener {
            context.openAnyUserProfile(data.user.id)
            dialog.dismiss()
        }

        // Blur
        UtilsUI.addBlur(holder.blurView, context)

        return view
    }

    /*
    This requests API to unlike comment (remove reaction).
     */
    private fun unlikeComment(holder: Holders.CommentHolder, data: Models.Comment) {
        ApiClient.runRequestWithoutParser(
            path = "api/comments/${data.id}/reactions",
            method = "DELETE",
        ) {
            data.alreadyReactedTo = false
            data.reactions = data.reactions - 1
            context.runOnUiThread {
                holder.reactionImageView.setColorFilter(context.getColor(R.color.AppLightGray))
                notifyDataSetChanged()
            }
        }
    }

    /*
    This requests API to like comment (add reaction).
     */
    private fun likeComment(holder: Holders.CommentHolder, data: Models.Comment) {

        // Create body
        val mediaType = "application/json".toMediaTypeOrNull()
        val reaction = Models.ReactionToComment(
            commentId = data.id,
            userId = context.currentUser.id
        )
        val bodyJson = Gson().toJson(reaction)
        val body = bodyJson.toRequestBody(mediaType)

        // Make request
        ApiClient.runRequestWithoutParser(
            path = "api/comments/reactions",
            method = "POST",
            body = body
        ) {
            data.reactions = data.reactions + 1
            data.alreadyReactedTo = true
            context.runOnUiThread {
                holder.reactionImageView.setColorFilter(Color.GREEN)
                notifyDataSetChanged()
            }
        }
    }
}
