package com.example.roverondo_mobile.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.roverondo_mobile.activities.GeneralActivity

abstract class AbstractAdapter<T, U>(
    val context: GeneralActivity,
    var dataSource: ArrayList<T> = ArrayList()
) : BaseAdapter() {

    // Variables & consts
    protected val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    /*
    This is main function for T objects. It initializes them using proper View Holder pattern.
     */
    abstract override fun getView(position: Int, convertView: View?, parent: ViewGroup): View

    /*
    This function initializes T objects -> usually sets up UI data and listeners.
     */
    protected abstract fun initRow(holder: U, view: View, data: T): View

    /*
    Returns T item from saved list.
     */
    override fun getItem(position: Int): T {
        return dataSource[position]
    }

    /*
    Returns id of item in saved list of T.
     */
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /*
    Returns number of T.
     */
    override fun getCount(): Int {
        return dataSource.size
    }
}