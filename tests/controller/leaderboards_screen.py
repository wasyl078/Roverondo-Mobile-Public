from time import sleep as wait
from selenium.webdriver.common.by import By
from controller.abstract_controller import AbstractController


class LeaderboardsScreen(AbstractController):
    def __init__(self, driver):
        super().__init__(driver)

        # Locators
        self.user_award_row = (
            By.ID, "com.example.roverondo_mobile:id/rowUserAwardTextView")

    def get_names_of_users(self):
        wait(3)
        self.wait_for(*self.user_award_row)
        users = self.driver.find_elements(*self.user_award_row)
        return [x.text for x in users]

    def click_profile(self, profile_name):
        wait(3)
        self.wait_for(*self.user_award_row)
        users = self.driver.find_elements(*self.user_award_row)
        for user in users:
            if profile_name in user.text:
                user.click()
                wait(3)
                return True
        return False
