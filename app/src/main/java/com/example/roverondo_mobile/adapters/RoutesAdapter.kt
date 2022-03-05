package com.example.roverondo_mobile.adapters

import android.annotation.SuppressLint
import android.app.Dialog
import android.view.View
import android.view.ViewGroup
import com.example.roverondo_mobile.R
import com.example.roverondo_mobile.activities.GeneralActivity
import com.example.roverondo_mobile.fragments.EventFragment
import com.example.roverondo_mobile.models.Models
import com.example.roverondo_mobile.utils.UtilsDates
import com.example.roverondo_mobile.utils.UtilsUI


class RoutesAdapter(
    context: GeneralActivity,
    val fragment: EventFragment,
    val dialog: Dialog,
) : AbstractAdapter<Models.Post, Holders.RouteHolder>(context) {

    /*
    Sorting routes in by date order -> from newest, to oldest.
     */
    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()
        dataSource.sortByDescending { it.createdAt }
    }

    /*
    This is main function for comments. It initializes them using View Holder pattern.
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        // Init
        val data = dataSource[position]
        var view = convertView
        val holder: Holders.RouteHolder?

        // Init view and holder
        when (view == null) {
            false -> holder = view.tag as Holders.RouteHolder
            true -> {
                view = inflater.inflate(R.layout.row_route, parent, false)
                holder = Holders.RouteHolder(
                    view.findViewById(R.id.rowRouteTitleTextView),
                    view.findViewById(R.id.rowRouteDescriptionTextView),
                    view.findViewById(R.id.rowRouteLocationTextView),
                    view.findViewById(R.id.rowRouteDistanceTextView),
                    view.findViewById(R.id.rowRouteDateTextView),
                    view.findViewById(R.id.rowRouteReactionsTextView),
                    view.findViewById(R.id.rowRouteBackgroundBlurLayout)
                )

                view.tag = holder
            }
        }

        return initRow(holder, view!!, data)
    }

    /*
    This function initializes route -> sets up UI data.
     */
    @SuppressLint("SetTextI18n")
    override fun initRow(
        holder: Holders.RouteHolder,
        view: View,
        data: Models.Post
    ): View {

        // Title
        holder.titleTextView.text = "Title: " + UtilsUI.parseNullString(data.title)

        // Description
        holder.descriptionTextView.text =
            "Description: " + UtilsUI.parseNullString(data.description)

        // Location
        holder.locationTextView.text =
            "Location: " + UtilsUI.parseNullString(data.plannedRoute?.route?.location)

        // Distance
        holder.distanceTextView.text = "Distance: " + UtilsUI.parseNullToKilometers(data.plannedRoute?.route?.distance)

        // Date
        holder.dateTextView.text = "Added at: ${UtilsDates.timestampToLongDate(data.createdAt)}"

        // Reactions
        holder.reactionsTextView.text = "Reactions: ${data.reactionsCount}"

        // Blur
        UtilsUI.addBlur(holder.blurView, context)

        // Listener
        holder.blurView.setOnClickListener {
            dialog.dismiss()
            fragment.setPlannedRoute(data)
        }

        return view
    }
}
