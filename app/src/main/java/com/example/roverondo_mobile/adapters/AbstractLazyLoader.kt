package com.example.roverondo_mobile.adapters

import android.widget.AbsListView


abstract class AbstractLazyLoader(
    private var loading: Boolean = true,
    protected var previousTotal: Int = 0,
    protected var page: Int = 0,
    protected var treshold: Int = 10
) : AbsListView.OnScrollListener {

    /*
    Overriden function that checks whether user is scrolling closer to end of list.
    If user is near end -> new portion of data is fetched and adapter is updated.
     */
    override fun onScroll(
        view: AbsListView,
        firstVisibleItem: Int,
        visibleItemCount: Int,
        totalItemCount: Int
    ) {
        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false
                previousTotal = totalItemCount
                page += 1
            }
        }

        if (!loading && firstVisibleItem + visibleItemCount >= totalItemCount - treshold) {
            loading = true
            loadMoreData()
        }
    }

    /*
    This method resets previous total value, page value and loading value.
     */
    fun reset(){
        previousTotal = 0
        page = 0
        loading = true
        loadMoreData()
    }

    /*
    This method removes one value from previousTotal.
     */
    fun removeOne(){
        previousTotal -= 1
    }

    /*
    This method calls fetching more data on new thread, and updating adapter on UI thread.
     */
    abstract fun loadMoreData()

    /*
    Overriden because it has to be overriden.
     */
    override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {}
}