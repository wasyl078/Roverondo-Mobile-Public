from time import sleep as wait
from selenium.webdriver.common.by import By
from controller.abstract_controller import AbstractController


class PostScreen(AbstractController):
    def __init__(self, driver):
        super().__init__(driver)

        # Locators
        self.joined_users_button = (
            By.ID,
            "com.example.roverondo_mobile:id/detailsEventUsersJoinedRelativeLayout")
        self.single_user = (
            By.ID, "com.example.roverondo_mobile:id/rowUserTextView")
        self.join_button = (
            By.ID, "com.example.roverondo_mobile:id/detailsEventJoinTextView")
        self.post_author = (
            By.ID, "com.example.roverondo_mobile:id/postUserTextView")

    def open_joined_users_section(self):
        return self.click_button(self.joined_users_button)

    def get_joined_users(self):
        if not self.wait_for(*self.single_user):
            return None

        users = self.driver.find_elements(*self.single_user)
        return [x.text for x in users]

    def join_event(self):
        join_text = self.get_text(self.join_button)
        if join_text == "Joined":
            return True
        else:
            return self.click_button(self.join_button)

    def leave_event(self):
        join_text = self.get_text(self.join_button)
        if join_text == "Join":
            return True
        else:
            return self.click_button(self.join_button)

    def get_post_author(self, idx):
        return self.get_text(self.post_author, idx=idx)
