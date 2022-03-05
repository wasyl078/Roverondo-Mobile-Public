import pytest
import os
import re
from time import sleep as wait
from controller.tools_controller import ToolsAppium


def test_login_logut(Tools: ToolsAppium):
    """
    This test verifies whether

    *** Test Procedure ***

    * Open app
    * Allow app access to location
    * Login to Auth0
    * Check profile name
    * Logout
    * Check button text

    *** Pass/Fail Criteria ***

    * User is logged with custom Auth0 profile
    * Username is cokowo9977
    * User is logged out after Logout button click

    """
    # Allow app access to location
    Tools.login.allow_access_to_location()

    # Login to Auth0
    Tools.login.click_continue_authorization()
    Tools.login.login_to_auth0()

    # Check profile name
    Tools.wall.open_drawer_menu()
    assert Tools.wall.get_profile_name_from_header() == "cokowo9977"

    # Logout
    Tools.wall.click_drawer_element("Logout")

    # Check button text
    assert Tools.login.get_continue_authorization_text() == "Continue authorization"


def test_ranked_list(Tools: ToolsAppium):
    """
    This test verifies whether ranked list is displayed in descending order.

    *** Test Procedure ***

    * Open app
    * Allow app access to location
    * Login to Auth0
    * Go to Leaderboards
    * Check order of first 5 users
    * Logout

    *** Pass/Fail Criteria ***

    * Users are sorted by descending Total distance traveled

    """
    # Allow app access to location
    Tools.login.allow_access_to_location()

    # Login to Auth0
    Tools.login.click_continue_authorization()
    Tools.login.login_to_auth0()

    # Go to Leaderboards
    Tools.wall.open_drawer_menu()
    Tools.wall.click_drawer_element("Leaderboards")

    # Check order of users
    distances = []
    for username in Tools.leaderboards.get_names_of_users():
        distance_full = re.match(r"\[\d*.\d* km\]", username).group()
        distance = float(distance_full.replace("[", "").replace(" km]", ""))
        distances.append(distance)
    assert all(distances[i] >= distances[i + 1] for i in range(len(distances)-1))

    # Logout
    Tools.wall.open_drawer_menu()
    Tools.wall.click_drawer_element("Logout")


def test_joining_events(Tools: ToolsAppium):
    """
    This test verifies whether it is possible to join an event and later leave
    it.

    *** Test Procedure ***

    * Open app
    * Allow app access to location
    * Login to Auth0
    * Go to Leaderboards
    * Go to Łukasz Kowalski profile
    * Follow Łukasz Kowalski profile
    * Go to Wall
    * Filter by event posts
    * Click first post
    * Reset Joined button
    * Check Joined button
    * Join event
    * Check joined users
    * Leave event
    * Check joined users
    * Logout

    *** Pass/Fail Criteria ***

    * User can join and leave event
    * Joining \ leaving affects joined users list

    """
    # Allow app access to location
    Tools.login.allow_access_to_location()

    # Login to Auth0
    Tools.login.click_continue_authorization()
    Tools.login.login_to_auth0()

    # Go to Leaderboards
    Tools.wall.open_drawer_menu()
    Tools.wall.click_drawer_element("Leaderboards")

    # Go to Łukasz Kowalski profile
    Tools.leaderboards.click_profile("Łukasz Kowalski")

    # Follow Łukasz Kowalski profile
    Tools.user.follow_user()

    # Go to Wall
    Tools.wall.open_drawer_menu()
    Tools.wall.click_drawer_element("Wall")

    # Filter by event posts
    Tools.wall.filter_wall("Events")

    # Click first post
    Tools.wall.click_post(idx=0)

    # Reset Joined button
    Tools.post.leave_event()

    # Check Joined button
    Tools.post.open_joined_users_section()
    users = Tools.post.get_joined_users()
    Tools.click_back_button()
    assert "cokowo9977" not in users

    # Join event
    Tools.post.join_event()

    # Check joined users
    Tools.post.open_joined_users_section()
    users = Tools.post.get_joined_users()
    Tools.click_back_button()
    assert "cokowo9977" in users

    # Leave event
    Tools.post.leave_event()

    # Check joined users
    Tools.post.open_joined_users_section()
    users = Tools.post.get_joined_users()
    Tools.click_back_button()
    assert "cokowo9977" not in users

    # Logout
    Tools.wall.open_drawer_menu()
    Tools.wall.click_drawer_element("Logout")


def test_profile_editing(Tools: ToolsAppium):
    """
    This test verifies whether it is possible to edit users bio, city, weight
    and gender.

    *** Test Procedure ***

    * Open app
    * Allow app access to location
    * Login to Auth0
    * Go to user profile
    * Click pencil icon
    * Change bio
    * Check whether bio was updated
    * Click gender
    * Change gender
    * Check whether gender was updated
    * Click city
    * Change city
    * Check whether city was updated
    * Click weight
    * Change weight
    * Check whether weight was updated
    * Logout

    *** Pass/Fail Criteria ***

    * Gender, city, bio and weight should easly be updated
    """
    # Allow app access to location
    Tools.login.allow_access_to_location()

    # Login to Auth0
    Tools.login.click_continue_authorization()
    Tools.login.login_to_auth0()

    # Go to user profile
    Tools.wall.open_drawer_menu()
    assert Tools.wall.go_to_user_profile()

    # Change bio
    oldBio = Tools.user.get_bio()
    Tools.user.open_bio()
    newBio = "0" if oldBio == "1" else "1"
    Tools.user.change_universal(newBio)
    Tools.user.click_save()

    # Check whether bio was updated
    wait(2)
    assert Tools.user.get_bio() == newBio

    # Change gender
    oldGender = Tools.user.get_gender()
    Tools.user.open_gender()
    newGender = "MALE" if oldGender == "FEMALE" else "FEMALE"
    Tools.user.change_gender(newGender)

    # Check whether gender was updated
    wait(2)
    assert Tools.user.get_gender() == newGender

    # Change city
    oldCity = Tools.user.get_city()
    Tools.user.open_city()
    newCity = "0" if oldCity == "1" else "1"
    Tools.user.change_universal(newCity)
    Tools.user.click_save()

    # Check whether city was updated
    wait(2)
    assert Tools.user.get_city() == newCity

    # Change weight
    oldWeight = Tools.user.get_weight()
    Tools.user.open_weight()
    newWeight = "50.0" if oldWeight == "40.0kg" else "40.0"
    Tools.user.change_universal(newWeight)
    Tools.user.click_save()

    # Check whether weight was updated
    wait(2)
    assert Tools.user.get_weight() == newWeight+"kg"

    # Logout
    Tools.wall.open_drawer_menu()
    Tools.wall.click_drawer_element("Logout")


def test_follow_button(Tools: ToolsAppium):
    """
    This test verifies whether Follow button works properly.

    *** Test Procedure ***

    * Open app
    * Allow app access to location
    * Login to Auth0
    * Go to leaderboards
    * Go to honda.tanaka profile
    * Reset follow button
    * Follow honda.tanaka
    * Check follow button
    * Go to Wall
    * Check whether first post is made by honda.tanaka
    * Go to leaderboards
    * Go to honda.tanaka profile
    * Unfollow honda.tanaka
    * Check follow button
    * Go to Wall
    * Check whether there are no honda.tanaka posts
    * Logout

    *** Pass/Fail Criteria ***

    * Following user makes Follow button green (Followed) and there are this
      persons posts on wall
    * Unfollowing user makes Follow button red (Follow) and there are no more this
      persons posts on wall

    """
    # Allow app access to location
    Tools.login.allow_access_to_location()

    # Login to Auth0
    Tools.login.click_continue_authorization()
    Tools.login.login_to_auth0()

    # Go to Leaderboards
    Tools.wall.open_drawer_menu()
    Tools.wall.click_drawer_element("Leaderboards")

    # Go to honda.tanaka profile
    Tools.leaderboards.click_profile("honda.tanaka")

    # Reset follow button
    Tools.user.unfollow_user()

    # Follow honda.tanaka
    Tools.user.follow_user()

    # Check follow button
    assert Tools.user.get_follow_button_text() == "Followed"

    # Go to Wall
    Tools.wall.open_drawer_menu()
    Tools.wall.click_drawer_element("Wall")

    # Check whether first post is made by honda.tanaka
    assert "honda.tanaka" in Tools.post.get_post_author(0)

    # Go to leaderboards
    Tools.wall.open_drawer_menu()
    Tools.wall.click_drawer_element("Leaderboards")

    # Go to honda.tanaka profile
    Tools.leaderboards.click_profile("honda.tanaka")

    # Unfollow honda.tanaka
    Tools.user.unfollow_user()

    # Check follow button
    assert Tools.user.get_follow_button_text() == "Follow"

    # Go to Wall
    Tools.wall.open_drawer_menu()
    Tools.wall.click_drawer_element("Wall")

    # Check whether there are no honda.tanaka posts
    assert "honda.tanaka" not in Tools.post.get_post_author(0)

    # Logout
    Tools.wall.open_drawer_menu()
    Tools.wall.click_drawer_element("Logout")
