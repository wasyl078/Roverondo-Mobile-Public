package com.example.roverondo_mobile.adapters

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import com.example.roverondo_mobile.R
import com.example.roverondo_mobile.activities.GeneralActivity
import com.example.roverondo_mobile.models.Models
import com.example.roverondo_mobile.utils.UtilsUI
import com.example.roverondo_mobile.utils.UtilsUI.loadCircularImage

class UserAwardAdapter(context: GeneralActivity) :
    AbstractAdapter<Models.FullUser, Holders.UserHolderAward>(context) {

    /*
    This is main function for users. It initializes them using View Holder pattern.
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        // Init
        val data = dataSource[position]
        var view = convertView
        val holder: Holders.UserHolderAward?

        // Init view and holder
        when (view == null) {
            false -> holder = view.tag as Holders.UserHolderAward
            true -> {
                view = inflater.inflate(R.layout.row_user_award, parent, false)
                holder = Holders.UserHolderAward(
                    view.findViewById(R.id.rowUserAwardTextView),
                    view.findViewById(R.id.rowUserAwardImageView),
                    view.findViewById(R.id.rowUserAwardAwardImageView),
                    view.findViewById(R.id.rowUserAwardBackgroundBlurLayout)
                )
                view.tag = holder
            }
        }

        holder.position = position
        return initRow(holder, view!!, data)
    }

    /*
    This function initializes users -> sets up UI data and listeners.
     */
    override fun initRow(
        holder: Holders.UserHolderAward,
        view: View,
        data: Models.FullUser
    ): View {

        // User text
        holder.userTextView.text = data.nickname
        holder.userTextView.isSelected = true

        // User image
        holder.userImageView.loadCircularImage(data)

        // Listener
        holder.blurView.setOnClickListener {
            context.openAnyUserProfile(data.id)
        }

        // Award image
        when (holder.position) {
            0 -> {
                holder.awardImageView.visibility = View.VISIBLE
                holder.awardImageView.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.mipmap.ic_medal_first_place_foreground
                    )
                )
            }
            1 -> {
                holder.awardImageView.visibility = View.VISIBLE
                holder.awardImageView.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.mipmap.ic_medal_second_place_foreground
                    )
                )
            }
            2 -> {
                holder.awardImageView.visibility = View.VISIBLE
                holder.awardImageView.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.mipmap.ic_medal_third_place_foreground
                    )
                )
            }
            else -> holder.awardImageView.visibility = View.GONE
        }

        // Blur
        UtilsUI.addBlur(holder.blurView, context)

        return view
    }
}