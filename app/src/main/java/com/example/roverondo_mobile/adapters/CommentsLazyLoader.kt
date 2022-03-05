package com.example.roverondo_mobile.adapters

import android.widget.AbsListView
import com.example.roverondo_mobile.api.ApiClient
import com.example.roverondo_mobile.models.Models


class CommentsLazyLoader(
    private val adapter: CommentAdapter,
    private val postId: Int,
) : AbsListView.OnScrollListener, AbstractLazyLoader() {

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
            path = "api/posts/$postId/comments?page=$page&amount=${treshold}",
            method = "GET",
            modelClass = Array<Models.Comment>::class.java
        ) { newItems ->
            adapter.context.runOnUiThread {
                adapter.dataSource.addAll(newItems)
                adapter.notifyDataSetChanged()
            }
        }
    }
}
