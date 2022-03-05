package com.example.roverondo_mobile.adapters

import android.app.Dialog
import android.view.View
import android.view.ViewGroup
import com.example.roverondo_mobile.R
import com.example.roverondo_mobile.activities.GeneralActivity
import com.example.roverondo_mobile.models.Models
import com.example.roverondo_mobile.utils.UtilsUI
import com.example.roverondo_mobile.utils.UtilsUI.loadCircularImage

class UserAdapter(
    context: GeneralActivity,
    val dialog: Dialog? = null,
) : AbstractAdapter<Models.FullUser, Holders.UserHolder>(context) {

    /*
    This is main function for users. It initializes them using View Holder pattern.
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        // Init
        val data = dataSource[position]
        var view = convertView
        val holder: Holders.UserHolder?

        // Init view and holder
        when (view == null) {
            false -> holder = view.tag as Holders.UserHolder
            true -> {
                view = inflater.inflate(R.layout.row_user, parent, false)
                holder = Holders.UserHolder(
                    view.findViewById(R.id.rowUserTextView),
                    view.findViewById(R.id.rowUserImageView),
                    view.findViewById(R.id.rowUserBackgroundBlurLayout),
                )
                view.tag = holder
            }
        }

        return initRow(holder, view!!, data)
    }

    /*
    This function initializes users -> sets up UI data and listeners.
     */
    override fun initRow(
        holder: Holders.UserHolder,
        view: View,
        data: Models.FullUser
    ): View {

        // User text
        holder.userTextView.text = data.nickname

        // User image
        holder.userImageView.loadCircularImage(data)

        // Listener
        holder.blurView.setOnClickListener {
            dialog?.dismiss()
            context.openAnyUserProfile(data.id)
        }

        // Blur
        UtilsUI.addBlur(holder.blurView, context)

        return view
    }
}