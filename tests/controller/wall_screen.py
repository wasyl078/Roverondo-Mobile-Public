from time import sleep as wait
from selenium.webdriver.common.by import By
from controller.abstract_controller import AbstractController


class WallScreen(AbstractController):
    def __init__(self, driver):
        super().__init__(driver)

        # Locators
        self.image_button = (By.CLASS_NAME, "android.widget.ImageButton")
        self.image_view = (By.CLASS_NAME, "android.widget.ImageView")
        self.header_username = (
            By.ID, "com.example.roverondo_mobile:id/headerUserTextView")
        self.header_picture = (
            By.ID, "com.example.roverondo_mobile:id/headerUserImageView")
        self.drawer_element = (
            By.ID, "com.example.roverondo_mobile:id/design_menu_item_text")
        self.post_description = (
            By.ID, "com.example.roverondo_mobile:id/postDescriptionTextView")
        self.filter_title = (By.ID, "com.example.roverondo_mobile:id/title")

    def open_drawer_menu(self):
        return self.click_button(self.image_button)

    def get_profile_name_from_header(self):
        return self.get_text(self.header_username)

    def go_to_user_profile(self):
        return self.click_button(self.header_picture)

    def click_drawer_element(self, text):
        if not self.wait_for(*self.drawer_element):
            return False
        elements = self.driver.find_elements(*self.drawer_element)

        for elem in elements:
            if elem.text == text:
                elem.click()
                wait(3)
                return True

        return False

    def click_post(self, idx):
        if not self.wait_for(*self.post_description):
            return False

        descriptions = self.driver.find_elements(*self.post_description)
        descriptions[idx].click()
        wait(2)
        return True

    def filter_wall(self, criteria):
        if self.click_button(self.image_view):
            if self.wait_for(*self.filter_title):
                filter_buttons = self.driver.find_elements(*self.filter_title)
                for filter_button in filter_buttons:
                    if filter_button.text == criteria:
                        filter_button.click()
                        wait(5)
                        return True
        return False
