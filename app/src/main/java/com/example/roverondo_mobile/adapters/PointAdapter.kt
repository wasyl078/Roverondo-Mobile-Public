package com.example.roverondo_mobile.adapters

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import com.example.roverondo_mobile.R
import com.example.roverondo_mobile.activities.GeneralActivity
import com.example.roverondo_mobile.models.Models
import com.example.roverondo_mobile.utils.UtilsUI


class PointAdapter(context: GeneralActivity) :
    AbstractAdapter<Models.Point, Holders.PointHolder>(context) {

    /*
    This is main function for points. It initializes them using View Holder pattern.
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        // Init
        val data = dataSource[position]
        var view = convertView
        val holder: Holders.PointHolder?

        // Init view and holder
        when (view == null) {
            false -> holder = view.tag as Holders.PointHolder
            true -> {
                view = inflater.inflate(R.layout.row_point, parent, false)
                holder = Holders.PointHolder(
                    view.findViewById(R.id.rowPointNumberTextView),
                    view.findViewById(R.id.rowPointDataTextView),
                    view.findViewById(R.id.rowPointBackgroundBlurLayout)
                )
                view.tag = holder
            }
        }

        holder.position = position
        return initRow(holder, view!!, data)
    }

    /*
    This function initializes point -> sets up UI.
     */
    @SuppressLint("SetTextI18n")
    override fun initRow(
        holder: Holders.PointHolder,
        view: View,
        data: Models.Point
    ): View {

        // Point number
        holder.pointNumberTextView.text =
            context.getString(R.string.point) + (holder.position + 1).toString()

        // Point data
        holder.pointDataTextView.text = data.location

        // Blur
        UtilsUI.addBlur(holder.blurView, context)

        return view
    }
}
