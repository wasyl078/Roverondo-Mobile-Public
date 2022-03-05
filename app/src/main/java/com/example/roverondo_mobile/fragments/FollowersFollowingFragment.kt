package com.example.roverondo_mobile.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.roverondo_mobile.R
import com.example.roverondo_mobile.activities.GeneralActivity
import com.example.roverondo_mobile.adapters.FollowLazyLoader
import com.example.roverondo_mobile.adapters.UserAdapter
import com.example.roverondo_mobile.utils.Consts
import kotlinx.android.synthetic.main.fragment_followers_following.*

class FollowersFollowingFragment : Fragment(R.layout.fragment_followers_following) {

    // Variables & consts
    private lateinit var thisActivity: GeneralActivity
    private lateinit var adapter: UserAdapter
    private lateinit var lazyLoader: FollowLazyLoader
    private lateinit var title: String
    private lateinit var followInfo: String
    private var userId: Int? = null

    /*
    This "constructor" initializes UI.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Init variables
        thisActivity = activity as GeneralActivity
        thisActivity.invalidateOptionsMenu()
        title = arguments?.getString(Consts.TITLE)!!
        followInfo = arguments?.getString(Consts.FOLLOW)!!
        userId = arguments?.getInt(Consts.ID)!!

        // Init adapter
        initAdapter()
    }

    /*
    This function initializes adapter for followed / followers users list view.
     */
    private fun initAdapter() {
        adapter = UserAdapter(thisActivity)
        followersFollowingListView.adapter = adapter
        lazyLoader = FollowLazyLoader(adapter, "$userId/$followInfo")
        followersFollowingListView.setOnScrollListener(lazyLoader)
        adapter.notifyDataSetChanged()
        lazyLoader.onResult = { thisActivity.runOnUiThread { updateUI() } }
    }

    /*
    This function updates UI. If there are no following / followers then
    there is just "There are no followers/following" text in the middle of screen.
     */
    @SuppressLint("SetTextI18n")
    private fun updateUI() {
        when (adapter.dataSource.isEmpty()) {
            true -> { // No followers / followings
                followersFollowingEmptyTextView.visibility = View.VISIBLE
                followersFollowingEmptyTextView.text = "There are no $followInfo"
            }
            false -> { // There are some followers / followings
                followersFollowingEmptyTextView.visibility = View.GONE
            }
        }
    }

    /*
    This fragment should always change toolbar text to "Followers" / "Following".
     */
    override fun onResume() {
        super.onResume()
        thisActivity.updateToolbar(title)
    }
}