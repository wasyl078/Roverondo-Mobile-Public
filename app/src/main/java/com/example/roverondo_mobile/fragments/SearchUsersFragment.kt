package com.example.roverondo_mobile.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.roverondo_mobile.R
import com.example.roverondo_mobile.activities.GeneralActivity
import com.example.roverondo_mobile.adapters.UserAdapter
import com.example.roverondo_mobile.adapters.SearchUserLazyLoader
import com.example.roverondo_mobile.utils.UtilsUI
import kotlinx.android.synthetic.main.fragment_search_users.*

class SearchUsersFragment : Fragment(R.layout.fragment_search_users) {

    // Variables & consts
    private lateinit var thisActivity: GeneralActivity
    private lateinit var adapter: UserAdapter
    private lateinit var lazyLoaderSearch: SearchUserLazyLoader

    /*
    This "constructor" initializes search user fragment. It sets up UI, adapter and listeners.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Init variables
        thisActivity = activity as GeneralActivity
        thisActivity.invalidateOptionsMenu()

        // Blur
        UtilsUI.addBlur(dialogUserProfileEditingBackgroundBlurLayout, thisActivity)

        // Init listeners, adapter and UI
        initListeners()
        initAdapter()
    }

    /*
    This function initializes listeners (search button listener).
     */
    private fun initListeners() {

        // Listener for search icon
        searchUsersSearchImageView.setOnClickListener {

            // Get text from text field
            val searchText = searchUsersTextInputEditText.text.toString()

            // Init warnings or list of users
            if (searchText.length < 3)
                initUIWarnings()
            else
                initUIListOfUsers(searchText)

            // Hide keyboard after search button click
            thisActivity.hideKeyboard()
        }
    }

    /*
    This function initializes warning under search user text field.
     */
    private fun initUIWarnings() {
        // Middle screen text
        searchUsersEmptyTextView.visibility = View.VISIBLE
        searchUsersEmptyTextView.text = getString(R.string.start_searching)

        // List reset
        adapter.dataSource.clear()
        adapter.notifyDataSetChanged()

        // Error text
        searchUsersTextInputLayout.error = getString(R.string.text_must_be_at_least_3_chars)
        searchUsersTextInputLayout.isErrorEnabled = true
        searchUsersTextInputLayout.setErrorIconDrawable(R.drawable.ic_error)
        searchUsersTextInputLayout.boxStrokeColor = Color.RED
    }

    /*
    This function initializes list of users. It calls API for that list and then that list is
    displayed below search user text field.
     */
    private fun initUIListOfUsers(searchText: String) {
        lazyLoaderSearch.FILTER = searchText
        lazyLoaderSearch.reset()
        adapter.dataSource.clear()
        adapter.notifyDataSetChanged()
        lazyLoaderSearch.onResult =  {
            thisActivity.runOnUiThread {
                searchUsersTextInputLayout.isErrorEnabled = false
                searchUsersTextInputLayout.boxStrokeColor = thisActivity.getColor(R.color.AppGray)
                updateUI()
            }
        }
    }

    /*
    This function initializes adapter for user list view.
     */
    private fun initAdapter() {
        adapter = UserAdapter(thisActivity)
        searchUsersListView.adapter = adapter
        lazyLoaderSearch = SearchUserLazyLoader(adapter, null)
        searchUsersListView.setOnScrollListener(lazyLoaderSearch)
        adapter.notifyDataSetChanged()
    }

    /*
    This funcrion updates UI. When there are no users, then "0 users..." text field is displayed.
    If there are users in list, that list is displayed and previous text field is hidden.
     */
    @SuppressLint("SetTextI18n")
    private fun updateUI() {
        when (adapter.dataSource.isEmpty()) {
            true -> {
                searchUsersEmptyTextView.visibility = View.VISIBLE
                searchUsersEmptyTextView.text = "0 users found :("
            }
            false -> {
                searchUsersEmptyTextView.visibility = View.GONE
            }
        }
    }

    /*
    This fragment should always change toolbar text to "Search users"
     */
    override fun onResume() {
        super.onResume()
        thisActivity.updateToolbar(getString(R.string.search_users))
    }
}