package com.example.roverondo_mobile.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.roverondo_mobile.R
import com.example.roverondo_mobile.activities.GeneralActivity
import com.example.roverondo_mobile.adapters.PostAdapter
import com.example.roverondo_mobile.adapters.PostsLazyLoader
import com.example.roverondo_mobile.utils.*
import kotlinx.android.synthetic.main.fragment_wall.*


class WallFragment : Fragment(R.layout.fragment_wall) {

    // Variables & consts
    private lateinit var adapter: PostAdapter
    lateinit var lazyLoader: PostsLazyLoader
    private lateinit var thisActivity: GeneralActivity

    /*
    This "real constructor" for wall fragment downloads newest followed users posts and
    initializes list of posts which is then displayed to user.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        thisActivity = activity as GeneralActivity
        thisActivity.invalidateOptionsMenu()
        useFilter(Consts.FILTER_ALL)
    }

    /*
    This set-ups filter on Wall according to given filter parameter: All posts, only workouts
    post, only event posts or only shared route posts.
     */
    fun useFilter(filter: String) {
        adapter = PostAdapter(thisActivity, this)
        wallListView.adapter = adapter
        lazyLoader = PostsLazyLoader(adapter, filter, thisActivity.currentUser.id)
        wallListView.setOnScrollListener(lazyLoader)
    }

    /*
    This function add (if visible) "Empty" text view to screen.
     */
    fun addEmptyTextView() {
        if (wallListEmptyTextView != null) {
            wallListEmptyTextView.visibility = View.VISIBLE
        }
    }

    /*
    Setting proper toolbar title.
     */
    override fun onResume() {
        super.onResume()
        thisActivity.updateToolbar(getString(R.string.wall))
        thisActivity.updateNavigationCheckedItem(R.id.nav_wall)
    }
}
