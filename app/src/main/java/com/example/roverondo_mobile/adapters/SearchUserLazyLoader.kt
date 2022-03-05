package com.example.roverondo_mobile.adapters

import android.widget.AbsListView
import com.example.roverondo_mobile.api.ApiClient
import com.example.roverondo_mobile.models.Models


class SearchUserLazyLoader(
    private val adapter: UserAdapter,
    var FILTER: String?,
    var onResult: (() -> Unit)? = null
) : AbsListView.OnScrollListener, AbstractLazyLoader(treshold = 10) {

    // Variables & consts
    init {
        loadMoreData()
    }

    /*
    This method calls fetching more data on new thread, and updating adapter on UI thread.
     */
    override fun loadMoreData() {

        // Create request
        ApiClient.runRequestWithParser(
            path = "api/users/search/$FILTER?offset=$previousTotal&amount=$treshold",
            method = "GET",
            modelClass = Array<Models.FullUser>::class.java
        ) { newItems ->
            adapter.context.runOnUiThread {
                adapter.dataSource.addAll(newItems)
                adapter.notifyDataSetChanged()
                onResult?.invoke()
            }
        }
    }
}
