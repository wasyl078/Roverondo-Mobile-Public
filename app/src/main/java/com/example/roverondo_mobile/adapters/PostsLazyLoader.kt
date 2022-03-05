package com.example.roverondo_mobile.adapters

import android.widget.AbsListView
import com.example.roverondo_mobile.api.ApiClient
import com.example.roverondo_mobile.models.Models


class PostsLazyLoader(
    private val adapter: PostAdapter,
    private val FILTER: String,
    private val currentUserId: Int,
) : AbsListView.OnScrollListener, AbstractLazyLoader(treshold = 3) {

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
            path = "api/wall/$currentUserId?offset=$previousTotal&amount=$treshold&postTypes=$FILTER&extended=true",
            method = "GET",
            modelClass = Array<Models.Post>::class.java
        ) { newItems ->
            newItems.sortByDescending { it.createdAt }
            adapter.context.runOnUiThread {
                adapter.dataSource.addAll(newItems)
                adapter.notifyDataSetChanged()
            }
        }
    }
}
